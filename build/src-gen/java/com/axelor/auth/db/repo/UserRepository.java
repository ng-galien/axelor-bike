/*
 * Axelor Business Solutions
 * 
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 * 
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.auth.db.repo;

import com.axelor.auth.db.User;
import com.axelor.db.JpaRepository;
import com.axelor.db.Query;

public class UserRepository extends JpaRepository<User> {

	public UserRepository() {
		super(User.class);
	}

	public User findByName(String name) {
		return Query.of(User.class)
				.filter("self.name = :name")
				.bind("name", name)
				.fetchOne();
	}

	public User findByGroup(Long groupId) {
		return Query.of(User.class)
				.filter("self.group[].id = :groupId")
				.bind("groupId", groupId)
				.cacheable()
				.fetchOne();
	}

	public User findByGroup(String groupCode) {
		return Query.of(User.class)
				.filter("self.group[].code = :groupCode")
				.bind("groupCode", groupCode)
				.cacheable()
				.fetchOne();
	}

	public User findByEmail(String email) {
		return Query.of(User.class)
				.filter("self.email = :email")
				.bind("email", email)
				.cacheable()
				.fetchOne();
	}

	public User findByCode(String code) {
		return Query.of(User.class)
				.filter("LOWER(self.code) = LOWER(:code)")
				.bind("code", code)
				.fetchOne();
	}

}

