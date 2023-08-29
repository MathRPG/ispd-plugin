Template:

```
{type}{modifier}{impact} {commit message}.
```

## Type

Indicates the type of activity the commit refers to.

Commits should ideally contain _exactly_ one type of activity. (i.e., they should be _atomic_)

In the case of multiple activities, first cogitate introducing your changes in smaller steps.

If such is not possible, then either:
* Include only the most relevant one.
  * E.g., when functionality changes and refactoring occur, notate only functionality.
* Include all, if you deem all to be relevant. Common examples include:
  * dependency management and refactoring (to remove deprecated calls).
  * testing and environment (for instance, adding a dependency for testing).

The activity types recognized so far are as follows.

### r Refactoring

Changes in code structure that do not impact its observable behavior.
* Formatting and other aesthetic changes included.

### f Functionality

Changes in code behavior. Both bug fixes and new features included.

### t Testing

Changes related to automated testing.

### d External Documentation

Documentation outside of the source code. This includes:
* generated javadoc
* example files
* GitHub issue templates

### c Internal Documentation

Documentation inside of the source code (code comments and javadoc).

### e Environment

Changes to the development environment. This includes:
* upgrading java version
* dependency management
* changes to linting or formatting rules
  * other IDE settings, such as project modules
* changes to GitHub actions

## Modifier

An _optional_ plus (`+`) or minus (`-`) to indicate if the activity was incremental or subtractive.

Some activities are _implicitly_ incremental, such as functionality (f).

Commits that are _both_ incremental and subtractive should _probably_ be split in smaller commits.
* If such is not possible, usage of both modifiers is allowed (`+-`).

## Impact

An indication of the potential impact the commit has on the project, given via an estimate analysis.

Given a commit type `x`, its severity is notated, from lowest to highest potential impact:
```
x < X < X! < X!!
```

| Format | Impact | Examples |
| --- | --- | --- |
| `x` | None | most automated refactorings, typo fixes in source |
| `X` | Small | manual refactorings, moving/renaming a source file, adding a test case, changing UI text |
| `X!` | Medium | backwards-compatible changes, fixing minor bugs, adding a dependency |
| `X!!` | Large | backwards-incompatible changes, fixing major bugs, moving/renaming modules, removing functionality |

## Commit Message

Commit messages should be brief, and should _always_ end with a period (`.`).

Abbreviations for common elements, specially in refactoring commits, are allowed and encouraged.

| Abbreviation | Meaning |
| ---  | --- |
| cls  | class |
| cp   | copy |
| ext  | extract |
| impl | implement/implementation |
| inl  | inline |
| met  | method |
| mv   | move |
| pkg  | package |
| rnm  | rename |
| rm   | remove |
| un   | unused/unneeded |
| var  | variable |
