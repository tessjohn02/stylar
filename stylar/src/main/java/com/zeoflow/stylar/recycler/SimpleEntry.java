package com.zeoflow.stylar.recycler;

import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.zeoflow.stylar.Stylar;
import com.zeoflow.stylar.utils.NoCopySpannableFactory;

import org.commonmark.node.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.0.0
 */
@SuppressWarnings("WeakerAccess")
public class SimpleEntry extends StylarAdapter.Entry<Node, SimpleEntry.Holder>
{

    // small cache for already rendered nodes
    private final Map<Node, Spanned> cache = new HashMap<>();
    private final int layoutResId;
    private final int textViewIdRes;

    public SimpleEntry(@LayoutRes int layoutResId, @IdRes int textViewIdRes)
    {
        this.layoutResId = layoutResId;
        this.textViewIdRes = textViewIdRes;
    }

    /**
     * Create {@link SimpleEntry} that has TextView as the root view of
     * specified layout.
     */
    @NonNull
    public static SimpleEntry createTextViewIsRoot(@LayoutRes int layoutResId)
    {
        return new SimpleEntry(layoutResId, 0);
    }

    @NonNull
    public static SimpleEntry create(@LayoutRes int layoutResId, @IdRes int textViewIdRes)
    {
        return new SimpleEntry(layoutResId, textViewIdRes);
    }

    @NonNull
    @Override
    public Holder createHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent)
    {
        return new Holder(textViewIdRes, inflater.inflate(layoutResId, parent, false));
    }

    @Override
    public void bindHolder(@NonNull Stylar stylar, @NonNull Holder holder, @NonNull Node node)
    {
        Spanned spanned = cache.get(node);
        if (spanned == null)
        {
            spanned = stylar.render(node);
            cache.put(node, spanned);
        }
        stylar.setParsedMarkdown(holder.textView, spanned);
    }

    @Override
    public void clear()
    {
        cache.clear();
    }

    public static class Holder extends StylarAdapter.Holder
    {

        final TextView textView;

        protected Holder(@IdRes int textViewIdRes, @NonNull View itemView)
        {
            super(itemView);

            final TextView textView;
            if (textViewIdRes == 0)
            {
                if (!(itemView instanceof TextView))
                {
                    throw new IllegalStateException("TextView is not root of layout " +
                        "(specify TextView ID explicitly): " + itemView);
                }
                textView = (TextView) itemView;
            } else
            {
                textView = requireView(textViewIdRes);
            }
            this.textView = textView;
            this.textView.setSpannableFactory(NoCopySpannableFactory.getInstance());
        }
    }
}
