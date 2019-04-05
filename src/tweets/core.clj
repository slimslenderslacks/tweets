(ns tweets.core
  (:require [twitter.oauth :as oauth]
            [twitter.callbacks.handlers :as handlers]
            [twitter.api.streaming :as streaming]
            [cheshire.core :as json]
            [environ.core :refer [env]]
            [clj-http.client :as client])
  (:import (twitter.callbacks.protocols AsyncStreamingCallback)))

(def creds (oauth/make-oauth-creds
            ; consumer key
            (:consumer-key env)
            ; consumer secret
            (:consumer-secret env)
            ; user access token
            (:access-token env)
            ; user access token secret
            (:access-token-secret env)))

;; https://webhook.atomist.com/atomist/teams/ANEH67BKA/ingestion/TrackData/d61d4991-134a-43c4-ab21-7d2d0086a5f3

(defn on-bodypart
  "A streaming on-bodypart handler.
   baos is a ByteArrayOutputStream.  (str baos) is the response body (encoded as JSON).
   This handler will print the expanded media URL of Tweets that have media."
  [response baos]
  ;; parse the tweet (true means convert to keyword keys)
  (let [tweet (json/parse-string (str baos) true)
        ;; retrieve the media array from the tweet.
        ;; see https://dev.twitter.com/docs/tweet-entities
        media (get-in tweet [:entities :media])
        urls (map :expanded_url media)]
    (if tweet
      (let [data (json/generate-string
                  (merge
                   (select-keys tweet [:text :id])
                   (select-keys (:user tweet) [:description :name :screen_name])))]
        (println data)

        (clojure.pprint/pprint (client/post "https://webhook.atomist.com/atomist/teams/ANEH67BKA/ingestion/TrackData/d61d4991-134a-43c4-ab21-7d2d0086a5f3"
                             {:body data
                              :content-type :json}))))
    ;; doseq can include a conditional inline with its binding
    ;; we'll only iterate and print when urls isn't empty
    (doseq [url urls :when (not (empty? urls))]
      (println url))))

handlers/exception-print
;; print out API errors
handlers/get-twitter-error-message

;; construct a response handler:
;; see https://github.com/adamwynne/twitter-api/blob/master/src/twitter/callbacks/handlers.clj
;; for the handler code
(def async-streaming-callback
  (AsyncStreamingCallback.
   ;; our custom handler, it's called for each Tweet that comes in from the Streaming API.
   #'on-bodypart
   ; return the Twitter API error message on failure
   handlers/get-twitter-error-message
   ;; just print exceptions to the console when there's an exception
   handlers/exception-print))

(defn -main
  "The main function for this ns (like public static void main in Java).
  query is a command line argument and is used to filter tweets containing that String.
  Try dogs :) :) :)"
  [query]
  (println "Printing media urls for tweets containing" query)
  (streaming/statuses-filter :oauth-creds creds :callbacks async-streaming-callback :params {:track query}))
