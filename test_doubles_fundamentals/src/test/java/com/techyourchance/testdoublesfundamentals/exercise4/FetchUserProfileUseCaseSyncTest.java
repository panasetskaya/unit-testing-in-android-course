package com.techyourchance.testdoublesfundamentals.exercise4;

import com.techyourchance.testdoublesfundamentals.example4.networking.NetworkErrorException;
import com.techyourchance.testdoublesfundamentals.exercise4.FetchUserProfileUseCaseSync.UseCaseResult;
import com.techyourchance.testdoublesfundamentals.exercise4.networking.UserProfileHttpEndpointSync;
import com.techyourchance.testdoublesfundamentals.exercise4.users.User;
import com.techyourchance.testdoublesfundamentals.exercise4.users.UsersCache;

import static org.hamcrest.CoreMatchers.is;

import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;


public class FetchUserProfileUseCaseSyncTest {

    public static final String USER_ID = "abc";
    public static final User NON_INITIALIZED_USER = new User("", "", "");

    FetchUserProfileUseCaseSync SUT;

    UserProfileHttpEndpointSyncTd mUserProfileHttpEndpointSync;
    UsersCacheTd mUsersCache;

    @Before
    public void setUp() throws Exception {
        mUserProfileHttpEndpointSync = new UserProfileHttpEndpointSyncTd();
        mUsersCache = new UsersCacheTd();
        SUT = new FetchUserProfileUseCaseSync(mUserProfileHttpEndpointSync, mUsersCache);
    }

    @Test
    public void fetchUserProfile_success_successReturned() {
        UseCaseResult result = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertEquals(UseCaseResult.SUCCESS, result);
    }

    @Test
    public void fetchUserProfile_success_userProfileCached() {
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertEquals(USER_ID, mUsersCache.mUser.getUserId());
    }

    @Test
    public void fetchUserProfile_performed_userIdPassedToEndPoint() {
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(mUserProfileHttpEndpointSync.mUserId, is(USER_ID));
    }

    @Test
    public void fetchUserProfile_authError_failureReturned() {
        mUserProfileHttpEndpointSync.mIsAuthError = true;
        UseCaseResult result = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(result, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfile_authError_userProfileNotCached() {
        mUserProfileHttpEndpointSync.mIsAuthError = true;
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(mUsersCache.mUser, is(NON_INITIALIZED_USER));
    }

    @Test
    public void fetchUserProfile_serverError_failureReturned() {
        mUserProfileHttpEndpointSync.mIsServerError = true;
        UseCaseResult result = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(result, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfile_serverError_userProfileNotCached() {
        mUserProfileHttpEndpointSync.mIsServerError = true;
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(mUsersCache.mUser.getUserId(), is(NON_INITIALIZED_USER.getUserId()));
    }

    @Test
    public void fetchUserProfile_generalError_failureReturned() {
        mUserProfileHttpEndpointSync.mIsGeneralError = true;
        UseCaseResult result = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(result, is(UseCaseResult.FAILURE));
    }

    @Test
    public void fetchUserProfile_generalError_userProfileNotCached() {
        mUserProfileHttpEndpointSync.mIsGeneralError = true;
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(mUsersCache.mUser.getUserId(), is(NON_INITIALIZED_USER.getUserId()));
    }

    @Test
    public void fetchUserProfile_netWorkException_networkFailureReturned() {
        mUserProfileHttpEndpointSync.mIsNetworkError = true;
        UseCaseResult result = SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(result, is(UseCaseResult.NETWORK_ERROR));
    }

    @Test
    public void fetchUserProfile_netWorkException_userProfileNotCached() {
        mUserProfileHttpEndpointSync.mIsNetworkError = true;
        SUT.fetchUserProfileSync(USER_ID);
        Assert.assertThat(mUsersCache.mUser.getUserId(), is(NON_INITIALIZED_USER.getUserId()));
    }

    //---------------------------------------------------------------------------------------------
    // Helper classes

    private static class UserProfileHttpEndpointSyncTd implements UserProfileHttpEndpointSync {

        public String mUserId = "";
        public boolean mIsGeneralError;
        public boolean mIsAuthError;
        public boolean mIsServerError;
        public boolean mIsNetworkError;

        @Override
        public EndpointResult getUserProfile(String userId) throws NetworkErrorException {
            mUserId = userId;
            if (mIsAuthError) {
                return new EndpointResult(EndpointResultStatus.AUTH_ERROR, "", "", "");
            } else if (mIsGeneralError) {
                return new EndpointResult(EndpointResultStatus.GENERAL_ERROR, "", "", "");
            } else if (mIsServerError) {
                return new EndpointResult(EndpointResultStatus.SERVER_ERROR, "", "", "");
            } else if (mIsNetworkError) {
                throw new NetworkErrorException();
            } else {
                return new EndpointResult(EndpointResultStatus.SUCCESS, userId, "", "");
            }
        }
    }

    private static class UsersCacheTd implements UsersCache {

        User mUser = NON_INITIALIZED_USER;

        @Override
        public void cacheUser(User user) {
            mUser = user;
        }

        @Nullable
        @Override
        public User getUser(String userId) {
            if (Objects.equals(mUser.getUserId(), userId)) {
                return mUser;
            } else return null;
        }
    }

}