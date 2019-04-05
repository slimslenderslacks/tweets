(defproject tweets "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [twitter-api "0.7.4"]
                 [cheshire "5.2.0"]
                 [environ "1.1.0"]
                 [clj-http "3.9.1"]]

  :profiles {:dev {:source-paths ["dev/clj"]}
             :repl-options {:init-ns user}})
