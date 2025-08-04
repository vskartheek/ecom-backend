package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;


@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> categorypage=categoryRepository.findAll(pageDetails);
        List<Category> category = categorypage.getContent();
        if (category.isEmpty()) {
            throw new APIException("No categories found");
        } else {
            List<CategoryDTO> categoryDTOS=category.stream().map(cat->modelMapper.map(cat,CategoryDTO.class))
                    .toList();
            CategoryResponse categoryResponse=new CategoryResponse();
            categoryResponse.setContent(categoryDTOS);
            categoryResponse.setPageNumber(categorypage.getNumber());
            categoryResponse.setPageSize(categorypage.getSize());
            categoryResponse.setTotalElements(categorypage.getTotalElements());
            categoryResponse.setTotalPages(categorypage.getTotalPages());
            categoryResponse.setLastPages(categorypage.isLast());
            return categoryResponse;
        }
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.findByCategoryName(categoryDTO.getCategoryName()) != null) {
            throw new APIException(
                    "Category with the name '" + categoryDTO.getCategoryName() + "' already exists."
            );
        }
        // Map DTO -> Entity
        Category category = new Category();
        category.setCategoryName(categoryDTO.getCategoryName());

        // Save the new category and return as DTO
        Category savedCategory = categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = new CategoryDTO(
                savedCategory.getCategoryId(),
                savedCategory.getCategoryName()
        );

        return savedCategoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(long id) {
        Optional<Category> category=categoryRepository.findById(id);
        if(category.isEmpty()){
            throw new ResourceNotFoundException("category","categoryId",id);
        }
        categoryRepository.delete(category.get());
        return modelMapper.map(category.get(),CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        Optional<Category> categoryToUpdate=categoryRepository.findById(categoryId);
        if(categoryToUpdate.isEmpty()){
            throw new ResourceNotFoundException("category","categoryId","category with id "+categoryDTO.getCategoryId()+" not found");
        }
        Category categoryToUpdate1=categoryToUpdate.get();
        categoryToUpdate1.setCategoryName(categoryDTO.getCategoryName());
        Category category=categoryRepository.save(categoryToUpdate1);
        return modelMapper.map(category,CategoryDTO.class);
    }
}
