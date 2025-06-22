package com.ighost.demo.service;

import java.util.List;
import java.util.Optional; // 確保匯入 Optional

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ighost.demo.model.RoleDto;
import com.ighost.demo.repo.RoleRepository;

@Service
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	/**
	 * 根據關鍵字和分頁參數查詢角色列表。
	 * 
	 * @param keyword  搜尋關鍵字
	 * @param page     目前頁碼
	 * @param pageSize 每頁顯示的筆數
	 * @return 角色資料傳輸物件列表 (List of RoleDto)
	 */
	public List<RoleDto> findByKeyword(String keyword, int page, int pageSize) {
		// 根據頁碼和每頁筆數計算資料庫查詢的偏移量 (offset)
		int offset = (page - 1) * pageSize;
		return roleRepository.findByKeyword(keyword, offset, pageSize);
	}

	/**
	 * 根據關鍵字計算符合條件的角色總數。
	 * 
	 * @param keyword 搜尋關鍵字
	 * @return 符合條件的角色總數
	 */
	public int countByKeyword(String keyword) {
		return roleRepository.countByKeyword(keyword);
	}

	/**
	 * 根據角色 ID 查找單一角色的完整資訊，包括其擁有的所有功能。 主要用於角色編輯器載入資料。
	 * 
	 * @param roleId 要查詢的角色 ID
	 * @return 一個包含 RoleDto 的 Optional 物件，如果找不到則為空
	 */
	public Optional<RoleDto> findRoleById(String roleId) {
		return roleRepository.findById(roleId);
	}
	
	/**
     * 呼叫 Repository 層來儲存角色資料。
     * @param roleId 角色 ID
     * @param roleName 角色名稱
     * @param functionIds 功能 ID 列表
     */
    public void saveRole(String roleId, String roleName, List<Integer> functionIds) {
        // Service 層的職責是呼叫 Repository 完成資料庫操作
        roleRepository.save(roleId, roleName, functionIds);
    }
}