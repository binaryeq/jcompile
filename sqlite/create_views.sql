CREATE VIEW jep181_with_compiler AS
        SELECT DISTINCT
                project_name || '-' || project_major_version || '-' || project_minor_version || '-' || project_patch_version || '/' || class_1 AS fullpath,
                compiler_name_1 AS compiler_name,
                compiler_major_version_1 AS compiler_major_version,
                compiler_minor_version_1 AS compiler_minor_version,
                compiler_patch_version_1 AS compiler_patch_version,
                bytecode_jep181_1 AS bytecode_jep181
        FROM run32_EQ
        UNION
        SELECT DISTINCT 
                project_name || '-' || project_major_version || '-' || project_minor_version || '-' || project_patch_version || '/' || class_2 AS fullpath,
                compiler_name_2 AS compiler_name,
                compiler_major_version_2 AS compiler_major_version,
                compiler_minor_version_2 AS compiler_minor_version,
                compiler_patch_version_2 AS compiler_patch_version,
                bytecode_jep181_2 AS bytecode_jep181
        FROM run32_EQ
;
