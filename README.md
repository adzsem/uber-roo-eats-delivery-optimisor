# JEV Rider Optimiser

Pixel-first Android prototype for locally evaluating Deliveroo, Uber Eats and Just Eat courier offers.

## Current V1.1 setup

The optimiser is local-first and runs on the Pixel without a PC/VPS dependency.

The Uber parser now has a regression fixture for the observed notification format:

```
Delivery • £7.32
19 min (3.9 mi) away
```

That example resolves to:

- pay: £7.32
- distance: 3.9 mi
- displayed time: 19 min
- £/mile: ~£1.88
- displayed-estimate £/hour: ~£23.12
- required average speed: ~12.32 mph
- theoretical minimum ride time at the configured 19 mph bike cap: ~12.32 min
- local V1 score: 90/100 → ACCEPT

The bike maximum speed is configured centrally as **19 mph**.

## V1 principles

- Notification-first capture
- deterministic local scoring
- SQLite history
- optional JEV refinement
- optional VESC telemetry
- no automatic courier-app taps
- no private API interception
- no dependency on another machine

See `docs/V1.md` for the field-test plan.
