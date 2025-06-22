package com.ighost.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ighost.demo.entity.Role;

/**
 * Role 的 JPA Repository。 繼承 JpaRepository 來獲得標準 CRUD 功能。 繼承 RoleQueryRepository
 * 來獲得我們用 Criteria API 寫的複雜查詢。
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, String>, RoleQueryRepository {

	/**
	 * 使用 @EntityGraph 來解決 N+1 查詢問題。 當查詢一個 Role 時，會一併將其關聯的 functions 和 function 的
	 * group 抓取出來。
	 * 
	 * @param id 角色 ID
	 * @return 包含完整關聯資料的角色實體
	 */
	@Override
	@EntityGraph(attributePaths = { "functions", "functions.group" })
	Optional<Role> findById(String id);
}
