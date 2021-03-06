# Basic Usage


## Creating a `.proto` File

In the `resources/proto/examples` directory, the Clojure protobuf project
provides an example "person" protobuf:

```proto
package protobuf.examples.person;

option java_outer_classname = "Example";

message Person {
  required int32 id = 1;
  required string name = 2;
  optional string email = 3;
  repeated string likes = 4;
}
```


## Compiling `.proto` to `.java` and then to `.class`

A convenience script is provided to compile this:
`bin/compile-example-protobufs`. This will create a `.java` file which will be
saved in `target/examples`.

The `:test` profile in the `project.clj` file has added `target/examples` as
one of its `:java-source-paths` -- as such, lein will compile the `.java` files
in `target/examples` to `.class` so that they can be called from Clojure.

If you started the project REPL with the command `lein repl`, then all of this
has already been done for you.


## Using the Clojure API

From the dev REPL, pull in the core API and the compiled protobuf code:

```clj
[protobuf.dev] λ=> (require '[protobuf.core :as protobuf])
nil
[protobuf.dev] λ=> (import '(protobuf.examples.person Example$Person))
protobuf.examples.person.Example$Person
```

Now we can create a protobuf:

```clj
[protobuf.dev] λ=> (def p (protobuf/create
                            Example$Person
                            {:id 108
                             :name "Alice"
                             :email "alice@example.com"}))
#'protobuf.dev/p
[protobuf.dev] λ=> p
{:id 108, :name "Alice", :email "alice@example.com"}
```

The protocol buffer instance supports the usual Clojure operations:

```clj
[protobuf.dev] λ=> (assoc p :name "Alice B. Carol")
{:id 108, :name "Alice B. Carol", :email "alice@example.com"}
[protobuf.dev] λ=> (assoc p :likes ["climbing" "running" "jumping"])
{:id 108,
 :name "Alice",
 :email "alice@example.com",
 :likes ["climbing" "running" "jumping"]}
```

Additionally, converting between protobuf bytes and Clojure data is trivial:

```clj
[protobuf.dev] λ=> (def b (protobuf/->bytes p))
#'protobuf.dev/b
[protobuf.dev] λ=> b
#object["[B" 0x7e3a40eb "[B@7e3a40eb"]
[protobuf.dev] λ=> (protobuf/bytes-> p b)
{:id 108, :name "Alice", :email "alice@example.com"}
```

## Streams and Bytes

In addition to creating a protobuf instance from a Clojure map, Clojure
protobuf also supports passing the following to the `create` function:

* A Java byte array (i.e., of type `[B`)
* A `java.io.InputStream`
* A `com.google.protobuf.CodedInputStream`

Just as with creating one with a map, a protobuf class is expected as
the first parameter.
