package com.zeoflow.stylar.html;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zeoflow.stylar.StylarVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class StylarHtmlRendererImpl extends StylarHtmlRenderer
{

    private final boolean allowNonClosedTags;
    private final Map<String, TagHandler> tagHandlers;

    @SuppressWarnings("WeakerAccess")
    StylarHtmlRendererImpl(boolean allowNonClosedTags, @NonNull Map<String, TagHandler> tagHandlers)
    {
        this.allowNonClosedTags = allowNonClosedTags;
        this.tagHandlers = tagHandlers;
    }

    @Override
    public void render(
        @NonNull final StylarVisitor visitor,
        @NonNull StylarHtmlParser parser)
    {

        final int end;
        if (!allowNonClosedTags)
        {
            end = HtmlTag.NO_END;
        } else
        {
            end = visitor.length();
        }

        parser.flushInlineTags(end, new StylarHtmlParser.FlushAction<HtmlTag.Inline>()
        {
            @Override
            public void apply(@NonNull List<HtmlTag.Inline> tags)
            {

                TagHandler handler;

                for (HtmlTag.Inline inline : tags)
                {

                    // if tag is not closed -> do not render
                    if (!inline.isClosed())
                    {
                        continue;
                    }

                    handler = tagHandler(inline.name());
                    if (handler != null)
                    {
                        handler.handle(visitor, StylarHtmlRendererImpl.this, inline);
                    }
                }
            }
        });

        parser.flushBlockTags(end, new StylarHtmlParser.FlushAction<HtmlTag.Block>()
        {
            @Override
            public void apply(@NonNull List<HtmlTag.Block> tags)
            {

                TagHandler handler;

                for (HtmlTag.Block block : tags)
                {

                    if (!block.isClosed())
                    {
                        continue;
                    }

                    handler = tagHandler(block.name());
                    if (handler != null)
                    {
                        handler.handle(visitor, StylarHtmlRendererImpl.this, block);
                    } else
                    {
                        // see if any of children can be handled
                        apply(block.children());
                    }
                }
            }
        });

        parser.reset();
    }

    @Nullable
    @Override
    public TagHandler tagHandler(@NonNull String tagName)
    {
        return tagHandlers.get(tagName);
    }

    static class Builder
    {

        private final Map<String, TagHandler> tagHandlers = new HashMap<>(2);
        private boolean allowNonClosedTags;
        private boolean excludeDefaults;

        private boolean isBuilt;

        void allowNonClosedTags(boolean allowNonClosedTags)
        {
            checkState();
            this.allowNonClosedTags = allowNonClosedTags;
        }

        void addHandler(@NonNull TagHandler tagHandler)
        {
            checkState();
            for (String tag : tagHandler.supportedTags())
            {
                tagHandlers.put(tag, tagHandler);
            }
        }

        @Nullable
        TagHandler getHandler(@NonNull String tagName)
        {
            checkState();
            return tagHandlers.get(tagName);
        }

        public void excludeDefaults(boolean excludeDefaults)
        {
            checkState();
            this.excludeDefaults = excludeDefaults;
        }

        boolean excludeDefaults()
        {
            return excludeDefaults;
        }

        @NonNull
        public StylarHtmlRenderer build()
        {

            checkState();

            isBuilt = true;

            // okay, let's validate that we have at least one tagHandler registered
            // if we have none -> return no-op implementation
            return tagHandlers.size() > 0
                ? new StylarHtmlRendererImpl(allowNonClosedTags, Collections.unmodifiableMap(tagHandlers))
                : new StylarHtmlRendererNoOp();
        }

        private void checkState()
        {
            if (isBuilt)
            {
                throw new IllegalStateException("Builder has been already built");
            }
        }

        void addDefaultTagHandler(@NonNull TagHandler tagHandler)
        {
            for (String tag : tagHandler.supportedTags())
            {
                if (!tagHandlers.containsKey(tag))
                {
                    tagHandlers.put(tag, tagHandler);
                }
            }
        }
    }
}
