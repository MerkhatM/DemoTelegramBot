package com.example.DemoTelegramBot.repositories;

import com.example.DemoTelegramBot.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    @Query(value = "WITH RECURSIVE category_tree AS (\n" +
            "  SELECT id, name, parent_id\n" +
            "  FROM category\n" +
            "  WHERE parent_id IS NULL OR parent_id = 0\n" + // Добавлено условие для корневых категорий
            "  UNION ALL\n" +
            "  SELECT c.id, c.name, c.parent_id\n" +
            "  FROM category c\n" +
            "    JOIN category_tree ct ON c.parent_id = ct.id\n" +
            ")\n" +
            "SELECT * FROM category_tree;", nativeQuery = true)
    List<Category> getCategoryTree();


    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId")
    List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

    Category findByName(String name);
}
