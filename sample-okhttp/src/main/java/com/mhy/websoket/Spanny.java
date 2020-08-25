package com.mhy.websoket;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;

/**
 * 可扩展包装器，可轻松创建可扩展字符串.
 */
public class Spanny extends SpannableStringBuilder {

  private int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

  public Spanny() {
    super("");
  }

  public Spanny(CharSequence text) {
    super(text);
  }

  public Spanny(CharSequence text, Object... spans) {
    super(text);
    for (Object span : spans) {
      setSpan(span, 0, length());
    }
  }

  public Spanny(CharSequence text, Object span) {
    super(text);
    setSpan(span, 0, text.length());
  }

  /**
   * 将span对象设置为文本。这比创建Spanny
   * 或SpannableStringBuilder的新实例更有效。
   *
   * @return {@code SpannableString}.
   */
  public static SpannableString spanText(CharSequence text, Object... spans) {
    SpannableString spannableString = new SpannableString(text);
    for (Object span : spans) {
      spannableString.setSpan(span, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    return spannableString;
  }

  public static SpannableString spanText(CharSequence text, Object span) {
    SpannableString spannableString = new SpannableString(text);
    spannableString.setSpan(span, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableString;
  }

  /**
   * 附加字符序列{@code text}并在附加部分上跨{@code spans}.
   *
   * @param text 要追加的字符序列.
   * @param spans 一个或多个要跨越附加文本的对象.
   * @return this {@code Spanny}.
   */
  public Spanny append(CharSequence text, Object... spans) {
    append(text);
    for (Object span : spans) {
      setSpan(span, length() - text.length(), length());
    }
    return this;
  }

  public Spanny append(CharSequence text, Object span) {
    append(text);
    setSpan(span, length() - text.length(), length());
    return this;
  }

  /**
   * 将ImageSpan添加到文本的开头.
   *
   * @return this {@code Spanny}.
   */
  public Spanny append(CharSequence text, ImageSpan imageSpan) {
    text = "." + text;
    append(text);
    setSpan(imageSpan, length() - text.length(), length() - text.length() + 1);
    return this;
  }

  /**
   * 附加纯文本.
   *
   * @return this {@code Spanny}.
   */
  @Override
  public Spanny append(CharSequence text) {
    super.append(text);
    return this;
  }

  /**
   * @deprecated use {@link #append(CharSequence text)}
   */
  @Deprecated
  public Spanny appendText(CharSequence text) {
    append(text);
    return this;
  }

  /**
   * 更改标志。默认值为SPAN_EXCLUSIVE_EXCLUSIVE.
   * 这些标志确定在跨度范围的开头或结尾处插入文本时跨度的行为
   *
   * @param flag see {@link Spanned}.
   */
  public void setFlag(int flag) {
    this.flag = flag;
  }

  /**
   * 用指定的对象标记指定范围的文本。
   * 标志确定在跨度范围的开始或结尾处插入文本时跨度的行为。
   */
  private void setSpan(Object span, int start, int end) {
    setSpan(span, start, end, flag);
  }

  /**
   * 将span对象设置为spannable中指定文本的所有外观。
   * 每次迭代都必须提供一个span对象的新实例*因为它不能被重用。
   *
   * @param textToSpan 区分大小写的文本在当前跨度中跨度.
   * @param getSpan 接口以获取每个跨度字符串的跨度.
   * @return {@code Spanny}.
   */
  public Spanny findAndSpan(CharSequence textToSpan, GetSpan getSpan) {
    int lastIndex = 0;
    while (lastIndex != -1) {
      lastIndex = toString().indexOf(textToSpan.toString(), lastIndex);
      if (lastIndex != -1) {
        setSpan(getSpan.getSpan(), lastIndex, lastIndex + textToSpan.length());
        lastIndex += textToSpan.length();
      }
    }
    return this;
  }

  /**
   * 跨文本中的多个部分时返回一个新的跨度对象的接口.
   */
  public interface GetSpan {

    /**
     * @return 应该返回一个新的span对象.
     */
    Object getSpan();
  }
}