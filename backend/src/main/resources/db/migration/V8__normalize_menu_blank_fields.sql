-- 1.1.0 及以前的菜单弹窗会把未填写的可选字段以空串提交。
-- route_name 存在唯一索引，多个按钮节点的空串会互相冲突，这里统一改为 NULL。
UPDATE sys_menu SET route_name = NULL WHERE route_name = '';
UPDATE sys_menu SET path = NULL WHERE path = '';
UPDATE sys_menu SET component = NULL WHERE component = '';
UPDATE sys_menu SET permission = NULL WHERE permission = '';
UPDATE sys_menu SET icon = NULL WHERE icon = '';
UPDATE sys_menu SET external_link = NULL WHERE external_link = '';
UPDATE sys_menu SET text_badge = NULL WHERE text_badge = '';
UPDATE sys_menu SET active_path = NULL WHERE active_path = '';
