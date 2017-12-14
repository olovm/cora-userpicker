/*
 * Copyright 2016 Uppsala University Library
 *
 * This file is part of Cora.
 *
 *     Cora is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Cora is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Cora.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.uu.ub.cora.userpicker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UserInStorageUserPickerTest {
	private static final String FITNESSE_USER_ID = "121212";
	private static final String GUEST_ID = "12345";
	private UserPicker userPicker;
	private User user;

	private UserStorageSpy userStorage;

	@BeforeMethod
	public void setUp() {
		userStorage = new UserStorageSpy();
		userPicker = UserInStorageUserPicker.usingUserStorage(userStorage);
	}

	@Test
	public void testGuest() {
		user = userPicker.pickGuest();
		assertTrue(userStorage.getGuestUserIsCalled);
	}

	@Test
	public void testGuestInactive() {
		userStorage.setGuestToInactive();
		user = userPicker.pickGuest();
		assertNumberOfRoles(0);
	}

	@Test
	public void testUnknownUserIsGuest() {
		user = pickUserUsingIdInStorage("unknownUser");
		assertUserId(GUEST_ID);
		assertOnlyOneUserRole("guest");
	}

	private User pickUserUsingIdInStorage(String idInStorage) {
		UserInfo userInfo = UserInfo.withIdInUserStorage(idInStorage);
		User user = userPicker.pickUser(userInfo);
		return user;
	}

	private void assertUserId(String expectedUserId) {
		assertEquals(user.id, expectedUserId);
	}

	private void assertOnlyOneUserRole(String expectedRole) {
		assertFirstUserRole(expectedRole);
		assertOnlyOneUserRole();
	}

	private void assertFirstUserRole(String expectedFirstRole) {
		String firstRole = user.roles.iterator().next();
		assertEquals(firstRole, expectedFirstRole);
	}

	private void assertOnlyOneUserRole() {
		assertEquals(user.roles.size(), 1);
	}

	@Test
	public void testUserWithTwoRoles() {
		user = pickUserUsingIdInStorage(FITNESSE_USER_ID);
		assertUserId(FITNESSE_USER_ID);
		assertNumberOfRoles(2);
		assertUserRoles("fitnesse", "metadataAdmin");
	}

	private void assertNumberOfRoles(int numberOfRoles) {
		assertEquals(user.roles.size(), numberOfRoles);
	}

	private void assertUserRoles(String... userRoles) {
		int i = 0;
		for (String userRole : user.roles) {
			assertEquals(userRole, userRoles[i]);
			i++;
		}
	}

	@Test
	public void testInactiveUserReturnsGuest() {
		user = pickUserUsingIdInStorage("666666");
		assertUserId(GUEST_ID);
	}

	@Test
	public void testUnknownUserFromLoginIsGuest() {
		user = pickUserUsingIdFromLogin("unknown@ub.uu.se");
		assertUserId(GUEST_ID);
		assertOnlyOneUserRole("guest");
	}

	private User pickUserUsingIdFromLogin(String idFromLogin) {
		UserInfo userInfo = UserInfo.withLoginId(idFromLogin);
		User user = userPicker.pickUser(userInfo);
		return user;
	}

	@Test
	public void testUserFromLoginWithTwoRoles() {
		user = pickUserUsingIdFromLogin("fitnesse@ub.uu.se");
		assertUserId(FITNESSE_USER_ID);
		assertNumberOfRoles(2);
		assertUserRoles("fitnesse", "metadataAdmin");
	}

	@Test
	public void testInactiveUserFromLoginReturnsGuest() {
		user = pickUserUsingIdFromLogin("other@ub.uu.se");
		assertUserId(GUEST_ID);
		assertOnlyOneUserRole("guest");
	}
}
