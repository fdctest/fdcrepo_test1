/*
 * Copyright (C) 2013 Chris Lacy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.socialnetlib.android;

import org.appdotnet4j.model.AdnPost;
import org.appdotnet4j.model.AdnPostCompose;
import org.appdotnet4j.model.AdnPosts;
import org.appdotnet4j.model.AdnUser;
import org.tweetalib.android.model.TwitterUser;
import org.twitter4j.Twitter;

import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.ParameterMap;
import com.twitter.Validator;


public class AppdotnetApi extends SocialNetApi {

	/*
	 * 
	 */
	public AppdotnetApi(SocialNetConstant.Type type, String consumerKey, String consumerSecret) {
		super(type, consumerKey, consumerSecret);
		
		Validator.MAX_STATUS_LENGTH = Validator.MAX_APPDOTNET_POST_LENGTH;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	boolean isResponseValid(HttpResponse httpResponse) {
		int status = httpResponse.getStatus();
		if (status >= 200 && status < 300) {
	        return true;
		}
		return false;
	}
	
	BasicHttpClient getHttpClient() {
		
		BasicHttpClient httpClient = new BasicHttpClient("https://alpha-api.app.net");
        httpClient.addHeader("Authorization", "Bearer " + mCurrentOAuthToken);
        httpClient.setConnectionTimeout(2000);
        return httpClient;
	}
	
	BasicHttpClient getHttpClient(String accessToken) {
		
		BasicHttpClient httpClient = new BasicHttpClient("https://alpha-api.app.net");
        httpClient.addHeader("Authorization", "Bearer " + accessToken);
        httpClient.setConnectionTimeout(2000);
        return httpClient;
	}

	String doGet(String path, ParameterMap params) {
		return doGet(path, params, mCurrentOAuthToken);
	}
	
	String doGet(String path, ParameterMap params, String accessToken) {
		HttpResponse httpResponse = getHttpClient(accessToken).get(path, params);
		if (isResponseValid(httpResponse)) {
	        String body = httpResponse.getBodyAsString();
	        return body;
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.socialnetlib.android.SocialNetApi#verifyCredentialsSync(java.lang.String, java.lang.String)
	 */
	@Override
	public TwitterUser verifyCredentialsSync(String oAuthToken, String oAuthSecret) {
		
        String userString = doGet("/stream/0/users/me", null, oAuthToken);
        if (userString != null) {
        	AdnUser user = new AdnUser(userString);
        	return new TwitterUser(user);
        }
		
		return null;
	}
	
	/*
	 * 
	 */
	public TwitterUser getAdnUser(long userId) {
		
		String userString = doGet("/stream/0/users/" + userId, null);
        if (userString != null) {
        	AdnUser user = new AdnUser(userString);
        	return new TwitterUser(user);
        }
        
        return null;
	}
	
	/*
	 * 
	 */
	public AdnPosts getAdnStream() {
		return getPosts("/stream/0/posts/stream", null);
	}
	
	/*
	 * 
	 */
	public AdnPosts getAdnGlobalStream() {
		return getPosts("/stream/0/posts/stream/global", null);
	}
	
	/*
	 * 
	 */
	public AdnPosts getAdnMentions(int userId) {
		return getPosts("/stream/0/users/" + userId + "/mentions", null);
	}
	
	/*
	 * 
	 */
	public AdnPosts getAdnUserStream(int userId) {
		return getPosts("/stream/0/users/" + userId + "/posts", null);
	}
	
	/*
	 * 
	 */
	public AdnPosts getAdnTagPosts(String tag) {
		return getPosts("/stream/0/posts/tag/" + tag, null);
	}
	
	/*
	 * 
	 */
	private AdnPosts getPosts(String path, ParameterMap params) {
		String streamString = doGet(path, params);
        if (streamString != null) {
        	AdnPosts posts = new AdnPosts(streamString);
        	return posts;
        }
        
        return null;
	}
	
	/*
	 * 
	 */
	public AdnPost getAdnPost(long id) {
		String postString = doGet("/stream/0/posts/" + id, null);
        if (postString != null) {
        	return new AdnPost(postString);
        }
        return null;
	}
	
	public AdnPost setAdnStatus(AdnPostCompose compose) {
		BasicHttpClient httpClient = getHttpClient();
		ParameterMap params = httpClient.newParams().add("text", compose.mText);
		if (compose.mInReplyTo != null) {
			params = params.add("reply_to", compose.mInReplyTo.toString());
		}
		
		HttpResponse httpResponse = httpClient.post("/stream/0/posts", params);
		if (isResponseValid(httpResponse)) {
	        String postAsString = httpResponse.getBodyAsString();
	        if (postAsString != null) {
	        	return new AdnPost(postAsString);
	        }
		}
		
		return null;
	}

	@Override
	Twitter getAndConfigureApiInstance() {
		return null;
	}

	@Override
	void clearApiInstance() {
		// TODO Auto-generated method stub
		
	}
}
