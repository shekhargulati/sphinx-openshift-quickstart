package com.openshift.blog.web;

import com.openshift.blog.domain.Post;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

@Configurable
/**
 * A central place to register application converters and formatters. 
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		// Register application converters and formatters
	}

	public Converter<Post, String> getPostToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.openshift.blog.domain.Post, java.lang.String>() {
            public String convert(Post post) {
                return new StringBuilder().append(post.getTitle()).append(' ').append(post.getBody()).append(' ').append(post.getAuthor()).append(' ').append(post.getCreatedOn()).toString();
            }
        };
    }

	public Converter<Long, Post> getIdToPostConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.openshift.blog.domain.Post>() {
            public com.openshift.blog.domain.Post convert(java.lang.Long id) {
                return Post.findPost(id);
            }
        };
    }

	public Converter<String, Post> getStringToPostConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.openshift.blog.domain.Post>() {
            public com.openshift.blog.domain.Post convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Post.class);
            }
        };
    }

	public void installLabelConverters(FormatterRegistry registry) {
        registry.addConverter(getPostToStringConverter());
        registry.addConverter(getIdToPostConverter());
        registry.addConverter(getStringToPostConverter());
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
