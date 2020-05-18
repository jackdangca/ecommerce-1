package com.food.controller;

import com.food.entity.vo.ProductNameVM;
import com.food.entity.vo.ProductVO;
import com.food.exception.NoSuchEntityException;
import com.food.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {


    @Value("${product.list.page.size}")
    Integer pageSize;
    @Autowired
   ProductService service;

//    public ProductController(ProductService service) {
//        this.service = service;
//    }
    @PostMapping("/add")
    public ProductVO addProduct(@Valid @RequestBody ProductVO product){

        return service.addProduct(product);
    }
    @PostMapping("/update")
    public void updateProduct(@Valid @RequestBody ProductVO Product){
        service.updateProduct(Product);
    }

    @PostMapping("/delete/{id}")
    public void deleteProduct(@PathVariable("id") Integer id){

        service.deleteProductById(id);

    }
    @PostMapping("/get/{id}")
    public ProductVO getProduct(@PathVariable("id") Integer id){
        return service.getProductById(id).orElseThrow(()->new NoSuchEntityException("common.null.exist","product "+id));
    }
    @PostMapping("/findAll")
    public List<ProductVO> getAllProduct(){
        return service.getAll();
    }
    @PostMapping("/findAllNames")
    public List<ProductNameVM> getAllProductNames(){
        return service.getAllProductNames();
    }
    @PostMapping("/findAll/{page}")
    public Page<ProductVO> getAllProductByPage(@PathVariable(name="page") Integer page){
        return service.getAll(PageRequest.of(page,pageSize));
    }

    @PostMapping("/findByExample")
    public List<ProductVO> getAllProduct(@RequestBody ProductVO  product){
        return service.getAll(product);
    }
    @PostMapping("/findByCategoryId/{id}")
    public List<ProductVO> getAllProduct(@PathVariable Integer id){
        return service.getProductsByCategoryId(id);
    }

}
