package com.ighost.demo.repo;

import com.ighost.demo.entity.Role;
import java.util.List;

/**
 * 定義複雜的角色查詢方法，取代了原來的 RoleRepositoryCustom。
 */
public interface RoleQueryRepository {

	/**
	 * 使用 Criteria API 根據關鍵字和分頁查詢角色。
	 * 
	 * @param keyword  搜尋關鍵字
	 * @param offset   資料庫查詢偏移量
	 * @param pageSize 每頁筆數
	 * @return 角色實體列表
	 */
	List<Role> findByKeyword(String keyword, int offset, int pageSize);

	/**
	 * 使用 Criteria API 根據關鍵字計算角色總數。
	 * 
	 * @param keyword 搜尋關鍵字
	 * @return 符合條件的角色總數
	 */
	long countByKeyword(String keyword);
}
