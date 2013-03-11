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

package org.appdotnet4j.model;

import java.text.ParseException;
import java.util.Date;

import org.tjson.JSONArray;
import org.tjson.JSONException;
import org.tjson.JSONObject;
import org.tweetalib.android.TwitterManager;
import org.tweetalib.android.TwitterUtil;

public class AdnPost {

	public long mId;
	public Long mInReplyTo;
	public String mText;
	public AdnUser mUser;
	public Date mCreatedAt;
	public String mSource;
	public boolean mIsDeleted; 
	
	public AdnPost(String jsonAsString) {
		try {
			JSONObject object = new JSONObject(jsonAsString);
			mId = object.getLong("id");
			if (object.has("reply_to")) {
				try {
					// This value comes back as 'null' when no value. 
					mInReplyTo = object.getLong("reply_to");
				} catch (JSONException e) {}
			}
			
			// It's possible to have a status with no text (likely when items are deleted)
			mText = object.getString("text");
			if (mText == null) {
				mText = " ";
			}
			
			if (object.has("is_deleted")) {
				try {
					// This value comes back as 'null' when no value. 
					mIsDeleted = object.getBoolean("is_deleted");
				} catch (JSONException e) {}
			}

			String createdAtString = object.getString("created_at");
			mCreatedAt = TwitterUtil.iso6801StringToDate(createdAtString);
			
			if (object.has("user")) {
				String userString = object.getString("user");
				mUser = new AdnUser(userString);
			}
			
			if (object.has("source")) {
				JSONObject source = object.getJSONObject("source");
				mSource = source.getString("name");
			}
			
			if (object.has("entities")) {
				JSONObject entities = object.getJSONObject("entities");
				if (entities.has("mentions")) {
					JSONArray jsonArray = entities.getJSONArray("mentions");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject mention = jsonArray.getJSONObject(i);
						if (mention.has("id") && mention.has("name")) {
							Long id = mention.getLong("id");
							String username = mention.getString("name");
							// HACK
							TwitterManager.addUserIdentifier(username, id);
						}
					}
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
