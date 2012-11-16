package com.openshift.blog.web;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.joda.time.format.DateTimeFormat;
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxResult;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import com.openshift.blog.domain.Post;

@RequestMapping("/posts")
@Controller
@RooWebScaffold(path = "posts", formBackingObject = Post.class)
public class PostController {
	
	@RequestMapping(value="/search/{searchTerm}", method=RequestMethod.GET,produces = "text/html")
	public String search(@PathVariable("searchTerm")String searchTerm,Model uiModel) throws Exception{
		try{
			SphinxClient searchClient = new SphinxClient();
			String host = System.getenv("OPENSHIFT_INTERNAL_IP");
			int port = 15000;
			int mode = SphinxClient.SPH_MATCH_ALL;
			String index = "*";
			int offset = 0;
			int limit = 20;
			int sortMode = SphinxClient.SPH_SORT_RELEVANCE;
			String sortClause = "";
			
			searchClient.SetServer(host, port);
			searchClient.SetWeights ( new int[] { 100, 1 } );
			searchClient.SetMatchMode ( mode );
			searchClient.SetLimits ( offset, limit );
			searchClient.SetSortMode ( sortMode, sortClause );
			
			SphinxResult result = searchClient.Query(searchTerm, index);
			System.out.println("Result is "+result);
			String message =  "Query '" + searchTerm + "' retrieved " + result.total + " of " + result.totalFound + " matches in " + result.time + " sec." ;
			uiModel.addAttribute("message", message);
			uiModel.addAttribute("result", result);
			return "posts/result";
		}catch(Exception e){
			System.out.println(e.getCause());
			e.printStackTrace();
			return "posts/create";
		}
		
	}
	

	@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String create(@Valid Post post, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, post);
            return "posts/create";
        }
        uiModel.asMap().clear();
        post.persist();
        return "redirect:/posts/" + encodeUrlPathSegment(post.getId().toString(), httpServletRequest);
    }

	@RequestMapping(params = "form", produces = "text/html")
    public String createForm(Model uiModel) {
        populateEditForm(uiModel, new Post());
        return "posts/create";
    }

	@RequestMapping(value = "/{id}", produces = "text/html")
    public String show(@PathVariable("id") Long id, Model uiModel) {
        addDateTimeFormatPatterns(uiModel);
        uiModel.addAttribute("post", Post.findPost(id));
        uiModel.addAttribute("itemId", id);
        return "posts/show";
    }

	@RequestMapping(produces = "text/html")
    public String list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1) * sizeNo;
            uiModel.addAttribute("posts", Post.findPostEntries(firstResult, sizeNo));
            float nrOfPages = (float) Post.countPosts() / sizeNo;
            uiModel.addAttribute("maxPages", (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1 : nrOfPages));
        } else {
            uiModel.addAttribute("posts", Post.findAllPosts());
        }
        addDateTimeFormatPatterns(uiModel);
        return "posts/list";
    }

	@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String update(@Valid Post post, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, post);
            return "posts/update";
        }
        uiModel.asMap().clear();
        post.merge();
        return "redirect:/posts/" + encodeUrlPathSegment(post.getId().toString(), httpServletRequest);
    }

	@RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, Post.findPost(id));
        return "posts/update";
    }

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Post post = Post.findPost(id);
        post.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/posts";
    }

	void addDateTimeFormatPatterns(Model uiModel) {
        uiModel.addAttribute("post_createdon_date_format", DateTimeFormat.patternForStyle("M-", LocaleContextHolder.getLocale()));
    }

	void populateEditForm(Model uiModel, Post post) {
        uiModel.addAttribute("post", post);
        addDateTimeFormatPatterns(uiModel);
    }

	String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
}
