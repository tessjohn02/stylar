package com.zeoflow.stylar.html.tag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.commonmark.node.Emphasis;

import java.util.Arrays;
import java.util.Collection;

import com.zeoflow.stylar.StylarConfiguration;
import com.zeoflow.stylar.RenderProps;
import com.zeoflow.stylar.SpanFactory;
import com.zeoflow.stylar.html.HtmlTag;

public class EmphasisHandler extends SimpleTagHandler {
    @Nullable
    @Override
    public Object getSpans(
            @NonNull StylarConfiguration configuration,
            @NonNull RenderProps renderProps,
            @NonNull HtmlTag tag) {
        final SpanFactory spanFactory = configuration.spansFactory().get(Emphasis.class);
        if (spanFactory == null) {
            return null;
        }
        return spanFactory.getSpans(configuration, renderProps);
    }

    @NonNull
    @Override
    public Collection<String> supportedTags() {
        return Arrays.asList("i", "em", "cite", "dfn");
    }
}