package com.service.catalog.catalog

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/catalogs")
class CatalogApi(
    val catalogFindService: CatalogFindService
) {

    @GetMapping
    fun getCatalogs(
        @PageableDefault(sort = ["id"], direction = Sort.Direction.DESC) pageAble: Pageable
    ) = catalogFindService.findAll(pageAble)

//    @GetMapping("/{productId}")
//    fun getCatalog() =
//        catalogFindService.f(pageAble)

}

@Service
@Transactional(readOnly = true)
class CatalogFindService(
    val catalogRepository: CatalogRepository
) {

    fun findAll(pageable: Pageable) = catalogRepository.findAll(pageable)

    fun findByProductId(productId: String) = catalogRepository.findByProductId(productId)
}


class CatalogResponse(catalog: Catalog) {
    val productId = catalog.productId
    val productName = catalog.productName
    val stock = catalog.stock
    val unitPrice = catalog.unitPrice
}