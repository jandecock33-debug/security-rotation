I have these 2 lines in the console:
2025-04-30 -> 2025-05-30 | riskOn=false | holdings=[IEF] | periodRet=-1,56% | equity=10277454,26
2025-05-30 -> 2025-06-30 | riskOn=true | holdings=[GLD, NVDA, EFA] | periodRet=5,99% | equity=10893254,00

So in the second line there are 3 new securities : GLD, NVDA, EFA

When is the moment I should buy these 3 positions and sell IEF?

How the backtest is treating it

In the code we wrote, for each period we do roughly:

At rebalanceDate (e.g. 2025-05-30)

Look at prices up to that date.

Decide riskOn/riskOff.

Decide the new holdings (here: [GLD, NVDA, EFA]).

We then compute performance from:

from = rebalanceDate (2025-05-30 close)

to = nextRebalanceDate (2025-06-30 close)

So the model assumes:

You sell the old position (IEF) and buy the new ones (GLD, NVDA, EFA) at the close of 2025-05-30,
then you hold them until the close of 2025-06-30.

How to do this in real trading

You’ve got two realistic choices:

End-of-month close trading (closest to the backtest)

Compute signals on the last trading day of the month (2025-05-30).

Place market-on-close (MOC) or limit-on-close orders:

Sell IEF

Buy GLD, NVDA, EFA
→ Execution happens at (or very near) the 2025-05-30 closing prices.

Next-day open trading (slightly more conservative / no look-ahead)

On 2025-05-30 after the close, you compute signals.

On the next trading day’s open (e.g. 2025-06-02), you:

Sell IEF

Buy GLD, NVDA, EFA

In that case, your backtest should really be changed to:

Use data up to 2025-05-30 to decide,

But measure returns from next trading day’s open to next rebalance.

<strong>So on 2025-05-30, after the close, the computer program already know tha tthe next 3 to buy are GLD, NVDA and EFA ?</strong>

Yes—with the way your backtest is written, by the end of 2025-05-30 (after the close) the program already knows that the next holdings should be [GLD, NVDA, EFA].

Think of the timeline like this:

What data the program sees

For the rebalance on 2025-05-30:

It uses all daily prices up to and including 2025-05-30:

that means it does see the close of 2025-05-30

it sees nothing beyond that date

Using that history, it:

computes the momentum scores,

checks the 200-day MA (risk-on/off),

ranks everything,

and decides:

“From the next period onward, I want to hold GLD, NVDA, EFA.”

So yes: after the close on 2025-05-30, the program can know “GLD, NVDA, EFA are the new top 3.”


