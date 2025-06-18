-- 1. 功能群組 table
CREATE TABLE function_group (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. 功能 table（用 group_id 外鍵）
CREATE TABLE functions (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    group_id INTEGER NOT NULL,
    url VARCHAR(100) NOT NULL,
    FOREIGN KEY (group_id) REFERENCES function_group(id) ON DELETE CASCADE
);

-- 3. 使用者 table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- 4. 角色 table
CREATE TABLE roles (
    id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- 5. 使用者與角色對應表 (多對多)
CREATE TABLE user_roles (
    user_id INTEGER NOT NULL,
    role_id VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 6. 角色與功能對應表 (多對多)
CREATE TABLE role_functions (
    role_id VARCHAR(20) NOT NULL,
    function_id INTEGER NOT NULL,
    PRIMARY KEY (role_id, function_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (function_id) REFERENCES functions(id) ON DELETE CASCADE
);
