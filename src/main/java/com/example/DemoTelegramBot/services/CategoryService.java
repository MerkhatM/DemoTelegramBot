package com.example.DemoTelegramBot.services;

import com.example.DemoTelegramBot.models.Category;
import com.example.DemoTelegramBot.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findChildrenByParentId(Long parentId) {
        return categoryRepository.findChildrenByParentId(parentId);
    }

    public List<Category> getTree() {
        return categoryRepository.getCategoryTree();
    }

    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public void deleteCategoryAndChildrem(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category != null) {
            List<Category> removingCategories = categoryRepository.findChildrenByParentId(id);
            removingCategories.forEach(category1 -> category1.setParent(null));
            categoryRepository.deleteAll(removingCategories);
            categoryRepository.delete(category);
        }


    }
}
