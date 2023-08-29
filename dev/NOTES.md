# Missing Programming Concepts

## Design Patterns

### `Wrapper<T>` or `Decorator<T>`

Utility methods `getWrapped() -> T`, `from(T)`; latter needs to be in a separate class to have inheritance.

### `Builder<T>`

Useful for fluent interfaces, specially for UI elements.

--

## Models

--

## Scheduling

`SchedulingDecision` needs to be a data object; `PartialSchedulingDecision`

Schedulers (as in, the simulated system components that do scheduling and allocation) need an _internal representation of the system_.
This needs to be encapsulated, and given an explicit representation in the code.
- How to compose this well? Attribute maps?

In the future, should be able to use plugin-defined attributes and mechanics.

What design pattern applies here?
- Strategy, Decorator?
- Decorator with Strategies for each method (or group of methods)
- 'Assembly Line' design pattern idea

`Chooser` and `Invalidator` classes applied on `PartialSchedulingDecision`
- Strategy could fill or invalidate multiple fields at a time

--

## Graphical Interface

`MutuallyExclusiveButtonGroup`

### File Editor

Abstraction to encapsulate logic present in MainWindow:
maintaining a file (and potentially undoable actions) and keeping track of what and when changes are made.

### Configuration Window

Logic about having a window with {Ok, Apply, Cancel} buttons.

Results should be an object **returned** from the window, rather than the window having direct access to what it is manipulating.
- Also allows for easier undoable-ness

--

## Other Classes and Utilities

### `Serializer<T>`

Converts `T` to and from a `String` object. Perhaps could be parameterized as `Serializer<T, U>` to define target form.

On this latter format, a more suitable name would be `Transformer`. Could be used in many places, like XML conversion.

A specific interesting use case is Icon -> Service Center. In this case, the transformer might need a `Context` element.
- Even if adding `Context` is not applicable, could still be used to convert IconicModel -> SimulableModel.

### Generic Graph Service Package

Many applicable places, such as:
- Plugin Dependency Management
- DAG Tasks
- Icon Connection
- Service Center Networking
- State management (FSA)

### `Template` class

Notion of 'binding' a key to a final value, or another template. Accompanying `TemplateKey` class, to avoid primitive obsession.

Could be nested inside themselves. Inner templates might get keys already bound by higher-level templates. (Or vice-versa?)

Currently thinking the final target value would be a `String`, but perhaps the class could be made more general than that.
- `Key` needs a unique identifier to be bound safely, and that's it.
- Target 'thing' only needs to be composable.

Binds do not need to happen instantaneously, things could be lazily evaluated. (Use of Suppliers)

Partial results might or might not be returnable; if so, need a way to nest `Key` within target `String` (or type).

Methods:
- `replace(Key, String)`, `replace(Key, Template)`
- `replaceAll(Map<Key, String | Template>)`
  - two methods? one method with two args? use of new concept `Either<L, R>`?
- `compile() -> String` : finish the template, if possible
  - On missing keys: throw error, return `Optional`, partial return?
- `validate() -> bool` : validate whether all `Key`s have been bound to a full value

Potential conflation of concerns: key management and the actual replacing within the target template.
- For `String`s, how to determine the presence of a `Key`? Regex?

Potential support for hierarchical keys: e.g. `{ispd.policy.count}`, `{ispd.policy.names}`
- Would need for generic concept for a 'key', could it be an Object, a Map?

### Charting Facility

Most likely accessible through a fluent interface of builder methods.

Need to study use cases in code; Factory pattern might be more applicable.

### Logging Facility

Logging _standards_ needs to be developed.
- Be careful to not revert to log-and-throw!

### ResourceManager

To manage resources such as external files, generated policies, etc.

### Translation Module

`BundleKey`: Wrapper around `String`, to aid against primitive obsession.

--

# Goal Package Structure

The current package structure packages by _layer_, rather than _feature_.

[Here](http://www.javapractices.com/topic/TopicAction.do?Id=205) is an article with very strong arguments in favor of the latter.

But for such, iSPD's features need to be clearly separated. After all, what can the app do?
- There is a CLI and GUI version
- There is the simulation motor and its simulable models
- There is iconic modelling via the GUI
- There is importing and exporting models with different formats
- ...
