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

import java.util.ArrayList;

import org.appdotnet4j.model.AdnUser;
import org.socialnetlib.android.SocialNetConstant.Type;
import org.tweetalib.android.ConnectionStatus;
import org.tweetalib.android.TwitterContentHandle;
import org.tweetalib.android.TwitterFetchBooleans;
import org.tweetalib.android.TwitterFetchLists;
import org.tweetalib.android.TwitterFetchStatus;
import org.tweetalib.android.TwitterFetchUser;
import org.tweetalib.android.TwitterFetchUsers;
import org.tweetalib.android.TwitterModifyStatuses;
import org.tweetalib.android.TwitterPaging;
import org.tweetalib.android.TwitterSignIn;
import org.tweetalib.android.TwitterFetchBooleans.FetchBooleansWorkerCallbacks;
import org.tweetalib.android.TwitterFetchLists.FetchListsWorkerCallbacks;
import org.tweetalib.android.TwitterFetchStatus.FetchStatusWorkerCallbacks;
import org.tweetalib.android.TwitterFetchUser.FetchUserWorkerCallbacks;
import org.tweetalib.android.TwitterFetchUsers.FetchUsersWorkerCallbacks;
import org.tweetalib.android.TwitterModifyStatuses.ModifyStatusesWorkerCallbacks;
import org.tweetalib.android.TwitterSignIn.SignInWorkerCallbacks;
import org.tweetalib.android.callback.TwitterFetchDirectMessagesFinishedCallback;
import org.tweetalib.android.callback.TwitterFetchStatusesFinishedCallback;
import org.tweetalib.android.fetch.TwitterFetchDirectMessages;
import org.tweetalib.android.fetch.TwitterFetchStatuses;
import org.tweetalib.android.fetch.TwitterFetchDirectMessages.FetchMessagesWorkerCallbacks;
import org.tweetalib.android.fetch.TwitterFetchStatuses.FetchStatusesWorkerCallbacks;
import org.tweetalib.android.model.TwitterDirectMessages;
import org.tweetalib.android.model.TwitterLists;
import org.tweetalib.android.model.TwitterStatus;
import org.tweetalib.android.model.TwitterStatusUpdate;
import org.tweetalib.android.model.TwitterStatuses;
import org.tweetalib.android.model.TwitterUser;
import org.tweetalib.android.model.TwitterUsers;
import org.twitter4j.Twitter;
import org.twitter4j.TwitterException;
import org.twitter4j.User;
import org.twitter4j.auth.RequestToken;

public abstract class SocialNetApi {
	
	public abstract void init();
	public abstract TwitterUser verifyCredentialsSync(String oAuthToken, String oAuthSecret);
	abstract Twitter getAndConfigureApiInstance();
	abstract void clearApiInstance();
	
	SocialNetConstant.Type mType;
	String mCurrentOAuthToken;
	String mCurrentOAuthSecret;
	String mAppConsumerKey;
	String mAppConsumerSecret;
	
	private TwitterFetchBooleans mFetchBooleans;
	private TwitterFetchDirectMessages mFetchDirectMessages;
	private TwitterFetchStatus mFetchStatus;
	private TwitterFetchStatuses mFetchStatuses;
	private TwitterFetchUser mFetchUser;
	private TwitterFetchUsers mFetchUsers;
	private TwitterFetchLists mFetchLists;
	private TwitterModifyStatuses mModifyStatuses;
	private TwitterSignIn mSignIn;
	private ConnectionStatus mConnectionStatus;
	
	SocialNetApi(SocialNetConstant.Type type, String consumerKey, String consumerSecret) {
		
		mType = type;
		mAppConsumerKey = consumerKey;
		mAppConsumerSecret = consumerSecret;
		
		init();
		
		initFetchBooleans();
		initFetchDirectMessages();
		initFetchStatus();
		initFetchStatuses();
		initFetchUser();
		initFetchUsers();
		initFetchLists();
		initModifyStatuses();
		initSignIn();
	}
	
	/*
	 * 
	 */
	private void initFetchStatus() {
		
		mFetchStatus = new TwitterFetchStatus();
		
		FetchStatusWorkerCallbacks callbacks = new FetchStatusWorkerCallbacks() {

			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}

			@Override
			public void addUser(User user) {
				cacheUser(user);
			}

			@Override
			public AppdotnetApi getAppdotnetApi() {
				return SocialNetApi.this.getAppdotnetApi();
			}
		};
		
		mFetchStatus.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initFetchStatuses() {
		
		mFetchStatuses = new TwitterFetchStatuses();
		
		FetchStatusesWorkerCallbacks callbacks = new FetchStatusesWorkerCallbacks() {

			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}

			@Override
			public void addUser(User user) {
				cacheUser(user);
			}
			
			@Override
			public void addUser(AdnUser user) {
				cacheUser(user);
			}

			@Override
			public AppdotnetApi getAppdotnetApi() {
				return SocialNetApi.this.getAppdotnetApi();
			}

		};
		
		mFetchStatuses.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initFetchBooleans() {
		mFetchBooleans = new TwitterFetchBooleans();
		
		FetchBooleansWorkerCallbacks callbacks = new FetchBooleansWorkerCallbacks() {

			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}
		};
		
		mFetchBooleans.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initFetchDirectMessages() {
		
		mFetchDirectMessages = new TwitterFetchDirectMessages();
		
		FetchMessagesWorkerCallbacks callbacks = new FetchMessagesWorkerCallbacks() {

			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}

			@Override
			public void addUser(User user) {
				cacheUser(user);
			}
		};
		
		mFetchDirectMessages.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initSignIn() {
		mSignIn = new TwitterSignIn();
		
		SignInWorkerCallbacks callbacks = new SignInWorkerCallbacks() {

			@Override
			public String getConsumerKey() {
				return mAppConsumerKey;
			}

			@Override
			public String getConsumerSecret() {
				return mAppConsumerSecret;
			}

			@Override
			public TwitterUser verifyCredentials(String accessToken, String accessTokenSecret) {
				return verifyCredentialsSync(accessToken, accessTokenSecret);
			}

			@Override
			public Type getType() {
				return mType;
			}
			
		};
		
		mSignIn.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initFetchUser() {
		mFetchUser = new TwitterFetchUser();
		
		FetchUserWorkerCallbacks callbacks = new FetchUserWorkerCallbacks() {

			@Override
			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}

			@Override
			public AppdotnetApi getAppdotnetApi() {
				return SocialNetApi.this.getAppdotnetApi();
			}
			
		};
		
		mFetchUser.setWorkerCallbacks(callbacks);
	}
	
	protected AppdotnetApi getAppdotnetApi() {
		
		if (mType == SocialNetConstant.Type.Appdotnet) {
			return (AppdotnetApi)this;
		}
		
		return null;
	}
	/*
	 * 
	 */
	private void initFetchUsers() {
		mFetchUsers = new TwitterFetchUsers();
		
		FetchUsersWorkerCallbacks callbacks = new FetchUsersWorkerCallbacks() {

			@Override
			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}
			
			@Override
			public void addUser(User user) {
				cacheUser(user);
			}

			@Override
			public TwitterUser getUser(Long userID) {
				return mFetchUser.getUser(userID, null, mConnectionStatus);
			}
		};
		
		mFetchUsers.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initFetchLists() {
		mFetchLists = new TwitterFetchLists();
		
		FetchListsWorkerCallbacks callbacks = new FetchListsWorkerCallbacks() {

			@Override
			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}
			
		};
		
		mFetchLists.setWorkerCallbacks(callbacks);
	}
	
	/*
	 * 
	 */
	private void initModifyStatuses() {
		mModifyStatuses = new TwitterModifyStatuses();
		
		ModifyStatusesWorkerCallbacks callbacks = new ModifyStatusesWorkerCallbacks() {

			@Override
			public Twitter getTwitterInstance() {
				return SocialNetApi.this.getAndConfigureApiInstance();
			}
			
		};
		
		mModifyStatuses.setWorkerCallbacks(callbacks);
	}

	/*
	 * 
	 */
	public boolean isAuthenticated() {
		Twitter twitter = getAndConfigureApiInstance();
		if (twitter == null) {
			return false;
		}

		try {
			twitter.getAccountSettings();
			return true;
		} catch (TwitterException e) {
			return false;
		}
	}

	/*
	 * 
	 */
	private void cacheUser(User user) {
		cacheUser(user, false);
	}
	
	/*
	 * 
	 */
	private void cacheUser(User user, boolean forceUpdate) {
		if (user != null) {
			mFetchUser.setUser(user, forceUpdate);
		}
	}
	
	/*
	 * 
	 */
	private void cacheUser(AdnUser user) {
		cacheUser(user, false);
	}
	
	/*
	 * 
	 */
	private void cacheUser(AdnUser user, boolean forceUpdate) {
		if (user != null) {
			mFetchUser.setUser(user, forceUpdate);
		}
	}
	
	/*
	 * 
	 */
	public void setOAuthTokenWithSecret(String oAuthToken, String oAuthSecret, boolean cancelPending) {
		
		if (oAuthToken == null && mCurrentOAuthToken == null) {
			return;
		}
		else if (oAuthToken != null && mCurrentOAuthToken != null && oAuthToken.equals(mCurrentOAuthToken) == true) {
			return;
		}
		//if (oAuthSecret == null && mCurrentOAuthSecret == null) {
		//	return;
		//}
		else if (oAuthSecret != null && mCurrentOAuthSecret != null && oAuthSecret.equals(mCurrentOAuthSecret) == true) {
			return;
		}
		
		if (cancelPending == true) {
			if (oAuthToken == null) {
				mFetchBooleans.clearCallbacks();
				mFetchLists.clearCallbacks();
				mFetchDirectMessages.clearCallbacks();
				mFetchStatus.clearCallbacks();
				mFetchStatuses.clearCallbacks();
				mFetchUser.clearCallbacks();
				mFetchUsers.clearCallbacks();
				mModifyStatuses.clearCallbacks();
				mSignIn.clearCallbacks();
			}
		}
		
		mCurrentOAuthToken = oAuthToken;
		mCurrentOAuthSecret = oAuthSecret;
		
		clearApiInstance();
		getAndConfigureApiInstance();
	}
	
	/*
	 * 
	 */
	public void setConnectionStatus(ConnectionStatus.Callbacks connectionStatusCallbacks) {
		mConnectionStatus = new ConnectionStatus(connectionStatusCallbacks);
	}
	
	/*
	 * 
	 */
	public ConnectionStatus getConnectionStatus() {
		return mConnectionStatus;
	}

	public TwitterStatuses getContentFeed(TwitterContentHandle handle) {
		return mFetchStatuses.getStatuses(handle);
	}
	
	/*
	 * 
	 */
	public void getAuthUrl(TwitterSignIn.GetAuthUrlCallback callback) {
		mSignIn.getAuthUrl(callback);
	}
	public void getOAuthAccessToken(RequestToken requestToken, String oauthVerifier, TwitterSignIn.GetOAuthAccessTokenCallback callback) {
		mSignIn.getOAuthAccessToken(requestToken, oauthVerifier, callback);
	}
	
	/*
	 * Will be null if no cached entry exists
	 */
	public TwitterUser getUser(Long userId) {
		return getUser(userId, null);
	}
	public TwitterUser getUser(Long userId, TwitterFetchUser.FinishedCallback callback) {
		TwitterUser cachedUser = mFetchUser.getUser(userId, callback, mConnectionStatus);
		return cachedUser;
	}
	public TwitterUser getUser(String screenName, TwitterFetchUser.FinishedCallback callback) {
		TwitterUser cachedUser = mFetchUser.getUser(screenName, callback, mConnectionStatus);
		return cachedUser;
	}
	
	public void verifyUser(TwitterFetchUser.FinishedCallback callback) {
		mFetchUser.verifyUser(callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public TwitterUsers getUsers(TwitterContentHandle contentHandle, TwitterPaging paging) {
		TwitterUsers cachedUsers = mFetchUsers.getUsers(contentHandle, paging);
		return cachedUsers;
	}
	public TwitterUsers getUsers(TwitterContentHandle contentHandle, TwitterPaging paging, TwitterFetchUsers.FinishedCallback callback) {
		TwitterUsers cachedUsers = mFetchUsers.getUsers(contentHandle, paging, callback, mConnectionStatus);
		return cachedUsers;
	}
	
	/*
	 * 
	 */
	public TwitterDirectMessages getDirectMessages(TwitterContentHandle contentHandle) {
		TwitterDirectMessages cachedMessages = mFetchDirectMessages.getDirectMessages(contentHandle);
		return cachedMessages;
	}
	public TwitterDirectMessages getDirectMessages(TwitterContentHandle contentHandle, TwitterPaging paging, TwitterFetchDirectMessagesFinishedCallback callback) {
		TwitterDirectMessages cachedMessages = mFetchDirectMessages.getDirectMessages(contentHandle, paging, callback, mConnectionStatus);
		return cachedMessages;
	}
	
	public void sendDirectMessage(long userId, String recipientScreenName, String statusText, TwitterFetchDirectMessagesFinishedCallback callback) {
		mFetchDirectMessages.sendDirectMessage(userId, recipientScreenName, statusText, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public void updateFriendship(String currentUserScreenName, TwitterUser userToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipUser(currentUserScreenName, userToUpdate, create, callback, mConnectionStatus);
	}
	public void updateFriendship(String currentUserScreenName, TwitterUsers usersToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipUsers(currentUserScreenName, usersToUpdate, create, callback, mConnectionStatus);
	}
	public void updateFriendshipScreenName(String currentUserScreenName, String screenNameToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipScreenName(currentUserScreenName, screenNameToUpdate, create, callback, mConnectionStatus);
	}
	public void updateFriendshipScreenNames(String currentUserScreenName, ArrayList<String> screenNamesToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipScreenNames(currentUserScreenName, screenNamesToUpdate, create, callback, mConnectionStatus);
	}
	public void updateFriendshipUserId(long currentUserId, long userIdToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipUserId(currentUserId, userIdToUpdate, create, callback, mConnectionStatus);
	}
	public void updateFriendshipUserIds(long currentUserId, ArrayList<Long> userIdsToUpdate, boolean create, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.updateFriendshipUserIds(currentUserId, userIdsToUpdate, create, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public void createBlock(long currentUserId, Long userId, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.createBlock(currentUserId, userId, callback, mConnectionStatus);
	}
	public void createBlock(long currentUserId, ArrayList<Long> userIds, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.createBlock(currentUserId, userIds, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public void reportSpam(long currentUserId, Long userId, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.reportSpam(currentUserId, userId, callback, mConnectionStatus);
	}
	public void reportSpam(long currentUserId, ArrayList<Long> userIds, TwitterFetchUsers.FinishedCallback callback) {
		mFetchUsers.reportSpam(currentUserId, userIds, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public TwitterLists getLists(int userId) {
		TwitterLists cachedLists = mFetchLists.getLists(userId, null);
		return cachedLists;
	}
	public TwitterLists getLists(int userId, TwitterFetchLists.FinishedCallback callback) {
		TwitterLists cachedLists = mFetchLists.getLists(userId, callback);
		return cachedLists;
	}
	public TwitterLists getLists(String screenName) {
		TwitterLists cachedLists = mFetchLists.getLists(screenName, null);
		return cachedLists;
	}
	public TwitterLists getLists(String screenName, TwitterFetchLists.FinishedCallback callback) {
		TwitterLists cachedLists = mFetchLists.getLists(screenName, callback);
		return cachedLists;
	}
	
	/*
	 * 
	 */
	public TwitterStatus getStatus(long statusId, TwitterFetchStatus.FinishedCallback callback) {
		return mFetchStatus.getStatus(statusId, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public void setStatus(TwitterStatusUpdate statusUpdate, TwitterFetchStatus.FinishedCallback callback) {
		mFetchStatus.setStatus(statusUpdate, callback, mConnectionStatus);
	}
	
	/*
	 * 
	 */
	public void setRetweet(long statusId, TwitterFetchStatus.FinishedCallback callback) {
		mFetchStatus.setRetweet(statusId, callback, mConnectionStatus);
	}

	/*
	 * 
	 */
	public void setFavorite(TwitterStatus status, boolean isFavorite, TwitterModifyStatuses.FinishedCallback callback) {
		mModifyStatuses.setFavorite(status, isFavorite, callback);
	}
	public void setFavorite(TwitterStatuses statuses, boolean isFavorite, TwitterModifyStatuses.FinishedCallback callback) {
		mModifyStatuses.setFavorite(statuses, isFavorite, callback);
	}
	
	/*
	 * 
	 */
	public void triggerFetchStatuses(TwitterContentHandle contentHandle, TwitterPaging paging, TwitterFetchStatusesFinishedCallback callback, int priorityOffset) {
		mFetchStatuses.trigger(contentHandle, paging, callback, mConnectionStatus, priorityOffset);
	}
	
	/*
	 * 
	 */
	public void cancelFetchStatuses(TwitterFetchStatusesFinishedCallback callback) {
		mFetchStatuses.cancel(callback);
	}
	
	/*
	 * 
	 */
	public void getFriendshipExists(String userScreenName, String userScreenNameToCheck, TwitterFetchBooleans.FinishedCallback callback) {
		mFetchBooleans.getFriendshipExists(userScreenName, userScreenNameToCheck, callback, mConnectionStatus);
	}

	
	public TwitterFetchLists 	getFetchListsInstance() 	{ return mFetchLists; }
	public TwitterFetchStatus 	getFetchStatusInstance() 	{ return mFetchStatus; }
	public TwitterFetchBooleans	getFetchBooleansInstance()	{ return mFetchBooleans; }
	public TwitterFetchUser 	getFetchUserInstance() 		{ return mFetchUser; }
	public TwitterFetchUsers	getFetchUsersInstance() 	{ return mFetchUsers; }
	public TwitterModifyStatuses	getSetStatusesInstance()	{ return mModifyStatuses; }
	public TwitterSignIn 		getSignInInstance() 		{ return mSignIn; }
}
