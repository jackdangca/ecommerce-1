package com.food.controller;

import com.food.model.vo.MerchantVO;
import com.food.model.vo.Page;
import com.food.model.vo.ProductNameVM;
import com.food.model.vo.ProductVO;
import com.food.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {


    @Value("${product.list.page.size}")
    Integer pageSize;
    @Autowired
    IProductService service;

//    public ProductController(ProductService service) {
//        this.service = service;
//    }

    @PreAuthorize("hasAnyRole('MERCHANT','ADMIN')")
    @PostMapping("/add")
    public ProductVO addProduct(@Valid @RequestBody ProductVO product) {
        MerchantVO principal = (MerchantVO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        product.setMerchant_id(principal.getId());
        return service.addProduct(product);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('MERCHANT','ADMIN')")
    public void updateProduct(@Valid @RequestBody ProductVO Product) {
        service.updateProduct(Product);
    }


    @PreAuthorize("hasAnyRole('MERCHANT','ADMIN')")

    @PostMapping("/delete/{id}")
    public void deleteProduct(@PathVariable("id") Integer id) {
        service.deleteProductById(id);
    }

    @PostMapping("/get/{id}")
    public ProductVO getProduct(@PathVariable("id") Integer id) {
        return service.getProductById(id);
    }

    @PostMapping("/findAll")
    public List<ProductVO> getAllProduct() {
        return service.getAll();
    }

    @PostMapping("/findAllNames")
    public List<ProductNameVM> getAllProductNames() {
        return service.getAllProductNames();
    }

    @PostMapping("/findAll/{page}/{pageSize}")
    public Page<ProductVO> getAllProductByPage(@PathVariable(name = "page") Integer page,
                                               @PathVariable(name = "pageSize") Integer pageSize,
                                               @RequestParam(name = "orderBy",required = false) String order,
                                               @RequestParam(name = "by",required = false) String by,
                                               @RequestParam(name = "mId") Integer merchantId,@RequestBody(required =false) ProductVO example) {
        Page<ProductVO> of = Page.of(page == null ? 0 : page, pageSize == null ? this.pageSize : pageSize, order,by);
        if(example ==null)
            example =new ProductVO();
        example.setMerchant_id(merchantId);
        System.out.println("123  " +order+ by);
        return service.getAll(example, of);
    }

    @PostMapping("/findByExample")
    public List<ProductVO> getAllProduct(@RequestBody ProductVO product) {
        return service.getAll(product);
    }

    @PostMapping("/findByCategoryId/{id}")
    public List<ProductVO> getAllProduct(@PathVariable Integer id) {
        return service.getProductsByCategoryId(id);
    }

}
