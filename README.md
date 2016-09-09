# datomic-local-installation

## What

**If you want a nicely set up local datomic database, then keep reading.**

These are scripts that will help you get a local (meaning isolated and dev-time) `datomic` installation going. There are a few pre-requisites, but the scripts will help you figure out what those are.

## Usage

### `accept.clj`

- Run `accept.clj` and when that passes with `All good` then you can continue. 
- This script will only pass once the environment has been set up adequately to continue.
- This script is not destructive, ie, it won't make any changes to the environment.

### `complect.clj`

- This will bootstrap the system and leave it in a state that is ready to start. This script will also terminate with `All good` if all the conditions are satisfied.
- Metaphorically similar to switing the ignition to **on** in your car.

### `start.clj`

- This will be like engaging the starter-motor to get the engine running. 
- After this script completes with `All good`, the system will be in the running state.

### `retire.clj` & `term.clj`

- not yet.
