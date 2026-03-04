(ns sasara.markdown
  (:import [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser]
           [com.vladsch.flexmark.util.data MutableDataSet]))

(def ^:private options
  (doto (MutableDataSet.)))

(def ^:private parser
  (.build (Parser/builder options)))

(def ^:private renderer
  (.build (HtmlRenderer/builder options)))

(defn md->html
  "Convert a Markdown string to HTML."
  [markdown-str]
  (when markdown-str
    (let [doc (.parse parser markdown-str)]
      (.render renderer doc))))
