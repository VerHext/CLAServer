/**
 * Copyright (C) 2015 Open WhisperSystems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.whispersystems.claserver;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.utils.SystemProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class Config {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  public String githubSecret = "";
  public String githubUserToken = "";
  public String githubOauthClientId = "";
  public String githubOauthClientSecret = "";
  public String baseUrl;
  public String whisperSystemsUrl;

  private Config() throws IOException {
    try {
      InputStream stream = getClass().getResourceAsStream("/development.json");
      ObjectMapper mapper = new ObjectMapper();
      Map secrets = mapper.readValue(mapper.getJsonFactory().createJsonParser(stream), Map.class);
      githubSecret = getProp(secrets, "github-webhook-secret");
      githubUserToken = getProp(secrets, "github-user-token");
      githubOauthClientId = getProp(secrets, "github-oauth-client-id");
      githubOauthClientSecret = getProp(secrets, "github-oauth-client-secret");
      baseUrl = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production ?
              "https://open-whisper-cla.appspot.com/cla-server" : getProp(secrets, "base-url");
      whisperSystemsUrl = getProp(secrets, "whispersystems-url");
    } catch (Exception ignored) {
    }
  }

  private String getProp(Map secrets, String name) throws EntityNotFoundException, IOException {
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
      return (String) datastore.get(KeyFactory.createKey("Secrets", name)).getProperties().get("key");
    } else {
      return (String) secrets.get(name);
    }
  }

  private static Config instance = null;

  public static Config getInstance() {
    if (instance == null) {
      try {
        instance = new Config();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return instance;
  }
}
