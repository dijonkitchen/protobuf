(ns protobuf.impl.flatland.core
  "This implementation takes its name from the original code for this project
  that was done under the flatland Github org and which pulled in several
  flatland libraries as dependencies.

  This namespace is an internal implementation detail not intended for
  developers. The API for working with all implementations of protocol
  buffer backends for this project is provided in the `protobuf.core`
  namespace."
  (:require
    [protobuf.common :as common]
    [protobuf.impl.flatland.map :as protobuf-map]
    [protobuf.impl.flatland.mapdef :as protobuf]
    [protobuf.syntax :as syntax])
  (:import
    (com.google.protobuf.CodedInputStream)
    (java.io.InputStream))
  (:gen-class
    :name protobuf.impl.flatland.core.FlatlandProtoBuf
    :implements [clojure.lang.Associative
                 clojure.lang.ILookup
                 clojure.lang.IPersistentCollection
                 clojure.lang.IPersistentMap
                 clojure.lang.Seqable
                 java.lang.Iterable]
    :init init
    :constructors {[java.lang.Class clojure.lang.APersistentMap] []
                   [java.lang.Class "[B"] []
                   [java.lang.Class com.google.protobuf.CodedInputStream] []
                   [java.lang.Class java.io.InputStream] []}
    :methods [^:static [schema [Object] Object]]
    :state contents
    :main false))

(defn- get-instance
  [wrapper data]
  (if (map? data)
    (protobuf/create wrapper data)
    (protobuf/parse wrapper data)))

(defn- wrap-all
  [protobuf-class java-wrapper instance]
  {:instance instance
   :java-wrapper java-wrapper
   :protobuf-class protobuf-class})

(defn -init
  [protobuf-class data]
  (let [wrapper (protobuf/mapdef protobuf-class)]
    [[] (wrap-all protobuf-class
                  wrapper
                  (get-instance wrapper data))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   clojure.lang.Associative   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -containsKey (:containsKey common/associative-behaviour))
(def -entryAt (:entryAt common/associative-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   clojure.lang.ILookup   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -valAt (:valAt common/lookup-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   clojure.lang.IPersistentCollection   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -cons (:cons common/persistent-collection-behaviour))
(def -count (:count common/persistent-collection-behaviour))
(def -empty (:empty common/persistent-collection-behaviour))
(def -equiv (:equiv common/persistent-collection-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   clojure.lang.IPersistentMap   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -assoc
  [this k v]
  (new protobuf.impl.flatland.core.FlatlandProtoBuf
    (common/get-class this)
    ((:assoc common/persistent-map-behaviour) this k v)))

(def -assocEx (:assocEx common/persistent-map-behaviour))

(defn -without
  [this k]
  (new protobuf.impl.flatland.core.FlatlandProtoBuf
    (common/get-class this)
    ((:without common/persistent-map-behaviour) this k)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   clojure.lang.Seqable   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -seq (:seq common/seqable-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   java.lang.Iterable   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -forEach (:forEach common/iterable-behaviour))
(def -iterator (:iterator common/iterable-behaviour))
(def -spliterator (:spliterator common/iterable-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   java.lang.Object   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def -toString (:toString common/printable-behaviour))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   protobuf.core.ProtoBufAPI   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn -schema
  [protobuf-class]
  (protobuf/mapdef->schema
   (protobuf/mapdef protobuf-class)))

(def behaviour
  {:->bytes (fn [this]
             (protobuf-map/->bytes (common/get-instance this)))
   :->schema (fn [this]
             (protobuf/mapdef->schema (common/get-wrapper this)))
   :bytes-> (fn [this bytes]
             (new protobuf.impl.flatland.core.FlatlandProtoBuf
               (common/get-class this)
               (protobuf/parse (common/get-wrapper this) bytes)))
   :syntax (fn [this]
             (syntax/format (.getSyntax (.file (common/get-wrapper this)))))
   :read (fn [this in]
          (new protobuf.impl.flatland.core.FlatlandProtoBuf
            (common/get-class this)
            (first
              (protobuf/read (common/get-wrapper this) in))))
   :write (fn [this out]
           (protobuf/write out (common/get-instance this)))})
