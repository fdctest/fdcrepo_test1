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

package org.tweetalib.android;

import java.util.ArrayList;
import java.util.HashMap;

import org.asynctasktex.AsyncTaskEx;

import org.tweetalib.android.TwitterConstant.UsersType;
import org.tweetalib.android.model.TwitterIds;
import org.tweetalib.android.model.TwitterUser;
import org.tweetalib.android.model.TwitterUsers;

import org.twitter4j.IDs;
import org.twitter4j.Paging;
import org.twitter4j.ResponseList;
import org.twitter4j.Twitter;
import org.twitter4j.TwitterException;
import org.twitter4j.User;

public class TwitterFetchUsers {

	private FetchUsersWorkerCallbacks mWorkerCallbacks;
	private HashMap<String, TwitterIds> mIdsHashMap;
	private Integer mFetchUsersCallbackHandle;
	private HashMap<Integer, FinishedCallback> mFinishedCallbackMap;

	/*
	 * 
	 */
	public void clearCallbacks() {
		mFinishedCallbackMap.clear();
	}
	
	/*
	 * 
	 */
	public interface FetchUsersWorkerCallbacks {
		
		public Twitter getTwitterInstance();
		public void addUser(User user);
		public TwitterUser getUser(Long userID);
	}
	
	/*
	 * 
	 */
	public interface FinishedCallbackInterface {
		
		public void finished(TwitterFetchResult result, TwitterUsers users);
		
	}
	
	/*
	 * 
	 */
	public abstract class FinishedCallback implements FinishedCallbackInterface {
		
		static final int kInvalidHandle = -1; 
		
		public FinishedCallback() {
			mHandle = kInvalidHandle;
		}
		
		void setHandle(int handle) {
			mHandle = handle;
		}
		
		private int mHandle;
	}
	
	/*
	 * 
	 */
	public TwitterFetchUsers() {
		mFinishedCallbackMap = new HashMap<Integer, FinishedCallback>();
		mFetchUsersCallbackHandle = 0;
		mIdsHashMap = new HashMap<String, TwitterIds>();

	}
	
	/*
	 * 
	 */
	public void setWorkerCallbacks(FetchUsersWorkerCallbacks callbacks) {
		mWorkerCallbacks = callbacks;
	}
	
	/*
	 * 
	 */
	FinishedCallback getFetchUsersCallback(Integer callbackHandle) {
		FinishedCallback callback = mFinishedCallbackMap.get(callbackHandle);
		return callback;
	}
	
	/*
	 * 
	 */
	void removeFetchUsersCallback(FinishedCallback callback) {
		if (mFinishedCallbackMap.containsValue(callback)) {
			mFinishedCallbackMap.remove(callback.mHandle);
		}
	}
	
	/*
	 * 
	 */
	Twitter getTwitterInstance() {
		return mWorkerCallbacks.getTwitterInstance();
	}
	
	/*
	 * 
	 */
	TwitterIds setUsers(TwitterContentHandle contentHandle, IDs ids) {
		TwitterIds twitterIds = getUserIds(contentHandle);
		twitterIds.add(ids);
		return twitterIds;
	}
	
	/*
	 * 
	 */
	TwitterIds getUserIds(TwitterContentHandle handle) {
		
		if (mIdsHashMap != null) {
			TwitterIds users = mIdsHashMap.get(handle.getKey());
			if (users == null) {
				mIdsHashMap.put(handle.getKey(), new TwitterIds());
			}
				
			return users;
		}
		
		return null;
	}
	
	/*
	 * 
	 */
	public TwitterUsers getUsers(TwitterContentHandle contentHandle, TwitterPaging paging) {
		
		TwitterIds ids = getUserIds(contentHandle);
		
		if (ids == null || ids.getIdCount() == 0) {
			return null;
		}
		
		TwitterUsers result = new TwitterUsers();
		for (int i = 0; i < ids.getIdCount(); i++) {
			TwitterUser user = mWorkerCallbacks.getUser(ids.getId(i));
			if (user != null) {
				result.add(user);
			}
		}
		
		return result;
	}
	/*
	 * 
	 */
	public TwitterUsers getUsers(TwitterContentHandle contentHandle, TwitterPaging paging, FinishedCallback callback, ConnectionStatus connectionStatus) {

		TwitterUsers result = getUsers(contentHandle, paging);
		if (result == null) {
			trigger(contentHandle, paging, callback, connectionStatus);
		} else {
			callback.finished(new TwitterFetchResult(true, null), result);
		}
		
		return result;
	}
	
	/*
	 * 
	 */
	public void trigger(TwitterContentHandle contentHandle, TwitterPaging paging, FinishedCallback callback, ConnectionStatus connectionStatus) {
		
		if (connectionStatus.isOnline() == false) {
			if (callback != null) {
				callback.finished(new TwitterFetchResult(false, connectionStatus.getErrorMessageNoConnection()), null);
			}
			return;
		}
		
		assert(mFinishedCallbackMap.containsValue(callback) == false);
		
		mFinishedCallbackMap.put(mFetchUsersCallbackHandle, callback);
		new FetchUsersTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Fetch Users",
				new FetchUsersTaskInput(mFetchUsersCallbackHandle, contentHandle, connectionStatus, paging));
		
		mFetchUsersCallbackHandle += 1;
	}
	
	/*
	 * 
	 */
	public void cancel(FinishedCallback callback) {
		
		removeFetchUsersCallback(callback);
	}
	
	/*
	 * 
	 */
	public void updateFriendshipUser(String currentUserScreenName, TwitterUser userToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		updateFriendshipUsers(currentUserScreenName, new TwitterUsers(userToUpdate), create, callback, connectionStatus);
	}
	public void updateFriendshipUsers(String currentUserScreenName, TwitterUsers usersToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		ArrayList<String> userScreenNames = new ArrayList<String>();
		for (int i = 0; i < usersToUpdate.getUserCount(); i++) {
			userScreenNames.add(usersToUpdate.getUser(i).getScreenName());
		}
		updateFriendshipScreenNames(currentUserScreenName, userScreenNames, create, callback, connectionStatus);
	}
	
	/*
	 * 
	 */
	public void updateFriendshipScreenName(String currentUserScreenName, String screenNameToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		ArrayList<String> userScreenNames = new ArrayList<String>();
		userScreenNames.add(screenNameToUpdate);
		updateFriendshipScreenNames(currentUserScreenName, userScreenNames, create, callback, connectionStatus);
	}
	private static int _mFriendshipCounter = 0;
	public void updateFriendshipScreenNames(String currentUserScreenName, ArrayList<String> userScreenNamesToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		
		if (connectionStatus.isOnline() == false) {
			if (callback != null) {
				callback.finished(new TwitterFetchResult(false, connectionStatus.getErrorMessageNoConnection()), null);
			}
			return;
		}
		
		_mFriendshipCounter += 1;
		TwitterContentHandle contentHandle = new TwitterContentHandle(new TwitterContentHandleBase(TwitterConstant.ContentType.USERS, TwitterConstant.UsersType.UPDATE_FRIENDSHIP), 
				currentUserScreenName, Integer.toString(_mFriendshipCounter));
		
		mFinishedCallbackMap.put(mFetchUsersCallbackHandle, callback);
		new FetchUsersTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Fetch friendship",
				new FetchUsersTaskInput(mFetchUsersCallbackHandle, contentHandle, connectionStatus, userScreenNamesToUpdate, null, create));
	}
	
	/*
	 * 
	 */
	public void updateFriendshipUserId(long currentUserId, Long userIdToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		ArrayList<Long> userIds = new ArrayList<Long>();
		userIds.add(userIdToUpdate);
		updateFriendshipUserIds(currentUserId, userIds, create, callback, connectionStatus);
	}
	public void updateFriendshipUserIds(long currentUserId, ArrayList<Long> userIdsToUpdate, boolean create, FinishedCallback callback, ConnectionStatus connectionStatus) {
		if (connectionStatus.isOnline() == false) {
			if (callback != null) {
				callback.finished(new TwitterFetchResult(false, connectionStatus.getErrorMessageNoConnection()), null);
			}
			return;
		}
		
		_mFriendshipCounter += 1;
		TwitterContentHandle contentHandle = new TwitterContentHandle(new TwitterContentHandleBase(TwitterConstant.ContentType.USERS, TwitterConstant.UsersType.UPDATE_FRIENDSHIP), 
				Long.toString(currentUserId), Integer.toString(_mFriendshipCounter));
		
		mFinishedCallbackMap.put(mFetchUsersCallbackHandle, callback);
		new FetchUsersTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Update Friendships",
				new FetchUsersTaskInput(mFetchUsersCallbackHandle, contentHandle, connectionStatus, null, userIdsToUpdate, create));
	}
	
	/*
	 * 
	 */
	private void createBlockOrReportSpam(UsersType usersType, long currentUserId, Long userId, FinishedCallback callback, ConnectionStatus connectionStatus) {
		ArrayList<Long> userIds = new ArrayList<Long>();
		userIds.add(userId);
		createBlockOrReportSpam(usersType, currentUserId, userIds, callback, connectionStatus);
	}
	
	private void createBlockOrReportSpam(UsersType usersType, long currentUserId, ArrayList<Long> userIds, FinishedCallback callback, ConnectionStatus connectionStatus) {
		if (connectionStatus.isOnline() == false) {
			if (callback != null) {
				callback.finished(new TwitterFetchResult(false, connectionStatus.getErrorMessageNoConnection()), null);
			}
			return;
		}
		
		_mFriendshipCounter += 1;
		TwitterContentHandle contentHandle = new TwitterContentHandle(new TwitterContentHandleBase(TwitterConstant.ContentType.USERS, usersType), 
				Long.toString(currentUserId), Integer.toString(_mFriendshipCounter));
		
		mFinishedCallbackMap.put(mFetchUsersCallbackHandle, callback);
		new FetchUsersTask().execute(AsyncTaskEx.PRIORITY_MEDIUM, "Block or Report",
				new FetchUsersTaskInput(mFetchUsersCallbackHandle, contentHandle, connectionStatus, userIds));
	}
	
	/*
	 * 
	 */
	public void reportSpam(long currentUserId, Long userId, FinishedCallback callback, ConnectionStatus connectionStatus) {
		createBlockOrReportSpam(UsersType.REPORT_SPAM, currentUserId, userId, callback, connectionStatus);
	}
	public void reportSpam(long currentUserId, ArrayList<Long> userIds, FinishedCallback callback, ConnectionStatus connectionStatus) {
		createBlockOrReportSpam(UsersType.REPORT_SPAM, currentUserId, userIds, callback, connectionStatus);
	}
	
	/*
	 * 
	 */
	public void createBlock(long currentUserId, Long userId, FinishedCallback callback, ConnectionStatus connectionStatus) {
		createBlockOrReportSpam(UsersType.CREATE_BLOCK, currentUserId, userId, callback, connectionStatus);
	}
	public void createBlock(long currentUserId, ArrayList<Long> userIds, FinishedCallback callback, ConnectionStatus connectionStatus) {
		createBlockOrReportSpam(UsersType.CREATE_BLOCK, currentUserId, userIds, callback, connectionStatus);
	}
	
	/*
	 * 
	 */
	class FetchUsersTaskInput {
		
		FetchUsersTaskInput(Integer callbackHandle, TwitterContentHandle contentHandle, ConnectionStatus connectionStatus ,TwitterPaging paging) {
			mCallbackHandle = callbackHandle;
			mContentHandle = contentHandle;
			mConnectionStatus = connectionStatus;
			mPaging = paging;
		}
		
		FetchUsersTaskInput(Integer callbackHandle, TwitterContentHandle contentHandle, ConnectionStatus connectionStatus, ArrayList<Long> userIds) {
			mCallbackHandle = callbackHandle;
			mContentHandle = contentHandle;
			mConnectionStatus = connectionStatus;
			mUserIds = userIds != null ? new ArrayList<Long>(userIds) : null;
		}
		
		FetchUsersTaskInput(Integer callbackHandle, 
							TwitterContentHandle contentHandle, 
							ConnectionStatus connectionStatus, 
							ArrayList<String> userScreenNames,
							ArrayList<Long> userIds,
							boolean createFriendship) {
			mCallbackHandle = callbackHandle;
			mContentHandle = contentHandle;
			mConnectionStatus = connectionStatus;
			mScreenNames = userScreenNames != null ? new ArrayList<String>(userScreenNames) : null;
			mUserIds = userIds != null ? new ArrayList<Long>(userIds) : null;
			mCreateFriendship = createFriendship;
		}
		
		Integer mCallbackHandle;
		TwitterContentHandle mContentHandle;
		TwitterPaging mPaging;
		ConnectionStatus mConnectionStatus;
		ArrayList<String> mScreenNames;
		ArrayList<Long> mUserIds;
		boolean mCreateFriendship;
	}
	
	/*
	 * 
	 */
	class FetchUsersTaskOutput {
		
		FetchUsersTaskOutput(TwitterFetchResult result, Integer callbackHandle, TwitterUsers users) {
			mResult = result;
			mCallbackHandle = callbackHandle;
			mUsers = users;
		}
		
		TwitterFetchResult mResult;
		Integer mCallbackHandle;
		TwitterUsers mUsers;
	}
	
	/*
	 * 
	 */
	class FetchUsersTask extends AsyncTaskEx<FetchUsersTaskInput, Void, FetchUsersTaskOutput> {

		@Override
		protected FetchUsersTaskOutput doInBackground(FetchUsersTaskInput... inputArray) {

			ResponseList<User> users = null;
			FetchUsersTaskInput input = inputArray[0];
			TwitterUsers twitterUsers = null;
			Twitter twitter = getTwitterInstance();
			String errorDescription = null;
			
			if (input.mConnectionStatus.isOnline() == false) {
				return new FetchUsersTaskOutput(new TwitterFetchResult(false, input.mConnectionStatus.getErrorMessageNoConnection()), input.mCallbackHandle, null);
			}
			
			if (twitter != null) {

				Paging paging = null;
				if (input.mPaging != null) {
					paging = input.mPaging.getT4JPaging();
				}
				/*
				else {
					paging = new Paging(-1);
					paging.setCount(40);
				}*/
				
				IDs userIds = null;
				
				try {
					UsersType usersType = input.mContentHandle.getUsersType();
					switch (usersType) {
						case FRIENDS:
						{
							userIds = twitter.getFriendsIDs(-1);
							setUsers(input.mContentHandle, userIds);
							break;
						}
						
						case FOLLOWERS:
						{
							userIds = twitter.getFollowersIDs(-1);
							setUsers(input.mContentHandle, userIds);
							break;
						}
						
						case RETWEETED_BY:
						{
							if (paging == null) {
								paging = TwitterPaging.createGetMostRecent(50).getT4JPaging();
							}
							long statusId = Long.parseLong(input.mContentHandle.getIdentifier());
							users = twitter.getRetweetedBy(statusId, paging);
							break;
						}
						
						case PEOPLE_SEARCH:
						{
							String searchTerm = input.mContentHandle.getScreenName();
							users = twitter.searchUsers(searchTerm, 0);
							break;
						}
						
						case UPDATE_FRIENDSHIP:
						{
							twitterUsers = new TwitterUsers();
							
							if (input.mScreenNames != null) {
								for (String screenName : input.mScreenNames) {
									User user = null;
									// We can't follow ourself...
									if (screenName.toLowerCase().equals(input.mContentHandle.getScreenName().toLowerCase()) == false) {
										if (input.mCreateFriendship) {
											user = twitter.createFriendship(screenName);
										} else {
											user = twitter.destroyFriendship(screenName);
										}
									}
									if (user != null) {
										twitterUsers.add(new TwitterUser(user));
									}
								}
							} else if (input.mUserIds != null) {
								
								long currentUserId = Long.parseLong(input.mContentHandle.getScreenName());
								
								for (Long userId : input.mUserIds) {
									User user = null;
									// We can't follow ourself...
									if (currentUserId != userId) {
										if (input.mCreateFriendship) {
											user = twitter.createFriendship(userId);
										} else {
											user = twitter.destroyFriendship(userId);
										}
									}
									if (user != null) {
										twitterUsers.add(new TwitterUser(user));
									}
								}
							}
							
							if (twitterUsers.getUserCount() == 0) {
								twitterUsers = null;
							}
							
							break;
						}
						
						case CREATE_BLOCK:
						case REPORT_SPAM:
						{
							twitterUsers = new TwitterUsers();
							long currentUserId = Long.parseLong(input.mContentHandle.getScreenName());
							for (Long userId : input.mUserIds) {
								User user = null;
								// We can't act on ourself...
								if (currentUserId != userId) {
									if (usersType == UsersType.CREATE_BLOCK) {
										user = twitter.createBlock(userId);
									} else if (usersType == UsersType.REPORT_SPAM) {
										user = twitter.reportSpam(userId);
									}
									if (user != null) {
										twitterUsers.add(new TwitterUser(user));
									}
								}
							}
							
							if (twitterUsers.getUserCount() == 0) {
								twitterUsers = null;
							}
							
							break;
						}
						
					}
					
					if (userIds != null) {
						// TODO: Clean this temp crap up!!!!
						long[] ids = userIds.getIDs();
						//int max = paging.getCount();
						int max = 40;
						int numberToFetch = Math.min(max, ids.length);
						long[] longArray = new long[numberToFetch];
						for (int i = 0; i < numberToFetch; i++) {
							longArray[i] = ids[i];
						}
						users = twitter.lookupUsers(longArray);
					}
					
				} catch (TwitterException e) {
					e.printStackTrace();
					errorDescription = e.getErrorMessage();
				}
				
				if (users != null && twitterUsers == null) {
					twitterUsers = new TwitterUsers();
					for (User user : users) {
						mWorkerCallbacks.addUser(user);
						twitterUsers.add(new TwitterUser(user));
					}
				}
			}

			return new FetchUsersTaskOutput(new TwitterFetchResult(errorDescription == null ? true : false, errorDescription), input.mCallbackHandle, twitterUsers);
		}

		@Override
		protected void onPostExecute(FetchUsersTaskOutput output) {
			
			FinishedCallback callback = getFetchUsersCallback(output.mCallbackHandle);
			if (callback != null) {
				callback.finished(output.mResult, output.mUsers);
				removeFetchUsersCallback(callback);
			}

			super.onPostExecute(output);
		}
	}
	
}
