package com.chosun.classicwave.openFeign.gutenberg;

        import com.chosun.classicwave.openFeign.gutenberg.response.BookSearchResponse;
        import org.springframework.cloud.openfeign.FeignClient;
        import org.springframework.web.bind.annotation.GetMapping;
        import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "gutenberg-copyright-search-service",
        url = "https://gutendex.com",
        configuration = FeignConfig.class
)
public interface GutenbergApiClient {

    @GetMapping("/books")
    BookSearchResponse searchBooks(@RequestParam(value = "search") String search,
                                   @RequestParam(value = "copyright") String copyright);
}
