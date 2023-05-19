# iSPD

## About

The *iconic Simulator of Parallel and Distributed systems* (iSPD) is a tool for simulating and studying the performance of distributed computation grids.

Modeling is done via a graphical user interface with iconic elements (hence the name).
Simulation is done via a discrete event system with queue networks.

More info about the project, how it works, and the theory behind it can be found in `docs/`.

## Compilation and Execution

The project uses **gradle** for both compilation and execution.
To compile and execute the program, open a terminal in the project folder and execute:

```./gradlew run```

Remember that gradle must be installed properly in your system to begin with.
The project uses **Java 17** for both development and execution.

## Project Status

The project's code has suffered from disperse and uncoordinated development (see details in the section **Plugin Architecture > Motivation** below).

Therefore, interwoven with the development activities aimed at implementing the *Plugin Architecture*, the project's code must be cleaned up and improved.

### High Priority

Activities that can be considered of high priority are listed below. These activities will aid future development immensely, thus their high prioritization.

#### Code Standardization

A set of standards for the project's code must be created, and followed through with all newly created code. Code already present in the project must be retroactively changed to conform to the newly developed standards.

This envelops:
- Code Formatting
- Naming Conventions (including the language in which code is written -- currently some of it is in English, some in Portuguese)

#### Code Documentation

All newly created code should be properly documented. Already existing, but improperly documented code should be updated to adhere to the decided standards.

#### Adding Automated Tests

A lot of the code requires manual testing to verify behavior. It would be ideal to have as much automated testing as possible, especially unit tests.

Furthermore, a code coverage goal should be established and aimed for.

#### Refactoring

Old code should be refactored to make it easier to work with. This includes:
- Updating solutions adequate for old Java versions, but which are obsoleted in newer ones
	- For instance, using *constants* where *Enums* would be preferred
- Removing code duplication, especially duplication caused by the grid-cloud conceptual overlap
- Decreasing code and component coupling, to make it easier to add automated tests
- Adding new or adapting existing abstractions, to move the project in the direction of the desired plugin-oriented architecture

### Medium Priority

#### Fixing Bugs

Bugs makes the simulator's results unreliable, which makes all aspects of development more difficult.
The workflow for bug-fixing should ideally be:
- Create a new branch
- Create an automated (preferably unit) test case that evidences the bug
- Refactor the code to make the bug 'obvious' and easy to fix
- Fix the bug, validate new behavior with tests
- Document bugfix
- Merge branch into its parent branch

#### Swapping Technologies for Modern Alternatives

Technologies chosen for the project were adequate for the contemporary state of the project back then. However, with the evolution of both the application (and the future trajectory traced for it) and the technological landscape, some of these solutions find themselves as inadequate nowadays.

Some of these potentially-dubious solutions include:
- The use of XML for serialization of most things (models, user preferences, ...)
- The use of JavaCC for dynamic generation of Java code

Discussion is needed to determine if they're worth maintaining in the project, and if deemed not so, substituted for more modern alternatives.

#### Merging Grid and Cloud Systems

The cloud modeling and simulation system was built on top of the grid system. However, the original grid code was not designed with enough abstraction to permit the construction of a 'parallel' simulation mode. Thus, the solution was to *substitute* the simulation solution to a cloud-oriented one.

This was "fine" at the time, since the grid and cloud-oriented versions existed as two separate projects. However, now with the merged version, a lot of code duplication ensued.

A conceptual and programmatic merging of the different simulation types needs to happen. A lot of abstraction needs to be introduced, so that this problem doesn't arise again.

### Low Priority

#### Updating External Documentation

External documentation (the ones present in the project folder `docs`) are, in general, about some design decisions for the application, such as the grammar for a parser.

Updating this documentation is of currently low priority, since:
- Well-engineered code should demonstrate these characteristics without the need for external documentation (at most, a javadoc should suffice)
- The program's functionality and design will most likely change throughout the development, and the external documentation is best left to be re-created once the project gets to a 'stable' state
- We could create systems to programmatically generate this external documentation, so it is always in-sync with the program's real functionality and design

#### Merge Remaining Project Versions

There are 3 other project versions with features not present in the current one. These are:
- PaaS-oriented cloud modeling
- DAG-based task modeling system
- Edge computing modeling and simulation

The priority to add these is currently low, since in theory by creating the plugin-based architecture, we could engineer the API to be able to support these needed features, without needing to modify most of the codebase.

#### Adding User-Oriented Features

With the state of the project, the addition of any moderately complex feature will most likely cause more problems than solve. It is best to focus on getting the code to a 'stable' state before thinking about user-oriented features.

## Contributing

If you'd like to contribute to the project, simply open a Pull Request. It will be reviewed and merged if deemed constructive, with immense gratitude.

Use the code style configurations available in `docs/codeStyle`.
- If you'd like to suggest changes to the project code style, simply open a new _issue_ with your suggestion and reasoning!

Make sure to read the **Project Status** section above to know what the project's priorities are right now, and focus on changes aimed at these goals.
- For instance, a PR adding a new user-oriented feature may be refused/ignored, if it relies too much on code which will most likely be changed. On the other hand, PRs that standardize, document and/or refactor code to bring it more in line with the project's vision will have a high probability of being accepted.

Keep your PRs small. This will also increase the odds of it being accepted and merged.

## Plugin Architecture

This repository is a fork from the main project found [here](https://github.com/gspd-unesp/ispd).
The goal of this repository is to maintain a separate version of the project code, re-structured to take into account a plugin system for future expansions.

### History

The project (iSPD) has quite a long and interesting history. The project started as a C terminal application, eventually being re-implemented in Java and evolving a graphical component for modeling.

Then, from this version, different students would 'fork' the project, creating their own version with their own additions for their separate graduation (or master's degree) theses. The different projects include:
- The addition of scheduling algorithms
- The *cloud* modeling and simulation system, built on top of the grid one
	- Started as IaaS-only, but PaaS-oriented modeling was eventually added
- Energy cost and efficiency calculations for grid systems
- Fault modeling and simulation
- A DAG-oriented task modeling system
- Edge computing modeling and simulation

However, due to the lack of a shared versioning system for all these students, the different projects were seldom merged together. Thus, for instance, if you wanted to use both *energy calculations* and *fault modeling* in the same simulations, you couldn't.

Then, one student (the creator of the DAG task modeling) took upon himself to merge the repositories **by hand** (his only choice, due to the lack of a VCS).
The result is this repository (more specifically, the repository this one is forked from). Due to the lack of code standards, the code is very inconsistent in style, documentation, and architecture.
The merge process was not easy in the slightest, and some artifacts from the process include introduced (and carried over) bugs and defects (to no fault of the brave student who did the merge, he did the best job possible given the circumstances).

### Motivation

But this brings us to the reasoning for the plugin architecture. Hypothetically, if the application (iSPD) had been built from the start with a plugin system in it, there would be little to no reason for each student to maintain a separate version/repository of their own.

Whenever a new addition would be desired, all the student would need to do would be to expand the plugin API to accommodate their needs, and then develop their plugin. This would localize all needed code changes to a few known locations.

Depending on the degree of abstraction permitted to plugins, even relatively complicated projects such as the addition of fault modeling and simulation could be done almost completely via plugins, with minimal changes required to the simulation motor (or other parts of the codebase) to accommodate the project's functional requirements.