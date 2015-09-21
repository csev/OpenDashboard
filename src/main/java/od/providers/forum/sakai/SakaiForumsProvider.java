/**
 * 
 */
package od.providers.forum.sakai;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import od.providers.ProviderData;
import od.providers.ProviderException;
import od.providers.ProviderOptions;
import od.providers.config.ProviderConfiguration;
import od.providers.forum.ForumsProvider;
import od.providers.sakai.BaseSakaiProvider;
import od.repository.ProviderDataRepositoryInterface;

import org.apache.commons.lang3.StringUtils;
import org.apereo.lai.impl.ForumImpl;
import org.apereo.lai.impl.MessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author ggilbert
 *
 */
@Component("forums_sakai")
public class SakaiForumsProvider extends BaseSakaiProvider implements ForumsProvider {

  private static final Logger log = LoggerFactory.getLogger(SakaiForumsProvider.class);
  private final String COLLECTION_URI = "/direct/topic/site/{ID}.json";
  private final String MESSAGES_URI = "/direct/forum_message/topic/{ID}.json";

  private static final String KEY = "forums_sakai";
  private static final String NAME = "Sakai Forums Web Service";
  private ProviderConfiguration providerConfiguration;
  @Autowired private ProviderDataRepositoryInterface providerDataRepositoryInterface;

  @PostConstruct
  public void init() {
    providerConfiguration = getDefaultSakaiProviderConfiguration();
  }

  @Override
  public List<ForumImpl> getForums(ProviderOptions options) {
    ProviderData providerData = providerDataRepositoryInterface.findByProviderKey(KEY);

    List<ForumImpl> f = null;

    String url = fullUrl(providerData, StringUtils.replace(COLLECTION_URI, "{ID}", options.getCourseId()));
    ResponseEntity<SakaiTopicCollection> messageResponse = restTemplate.getForEntity(url + "?_sessionId=" + getSakaiSession(providerData), SakaiTopicCollection.class);
    List<SakaiForum> forums = messageResponse.getBody().getTopic_collection();
    
    if (forums != null && !forums.isEmpty()) {
      f = new ArrayList<ForumImpl>();
      for (SakaiForum sakaiForum : forums) {
        f.add(sakaiForum.toForum());
      }
    }
    
    return f;
  }


  @Override
  public List<MessageImpl> getMessages(ProviderOptions options, final String topicId) throws ProviderException {
    ProviderData providerData = providerDataRepositoryInterface.findByProviderKey(KEY);

    List<MessageImpl> m = null;

    String url = fullUrl(providerData, StringUtils.replace(MESSAGES_URI, "{ID}", topicId));
    ResponseEntity<SakaiTopicMessageCollection> messageResponse = restTemplate.getForEntity(url + "?_sessionId=" + getSakaiSession(providerData), SakaiTopicMessageCollection.class);
    List<SakaiTopicMessage> messages = messageResponse.getBody().getForum_message_collection();
    
    if (messages != null && !messages.isEmpty()) {
      m = new ArrayList<MessageImpl>();
      for (SakaiTopicMessage sakaiTopicMessage : messages) {
        m.add(sakaiTopicMessage.toMessage());
      }
    }
    
    return m;
  }

  @Override
  public String getKey() {
    return KEY;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public ProviderConfiguration getProviderConfiguration() {
    return providerConfiguration;
  }

}
