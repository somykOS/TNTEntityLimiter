### TNTQueue
This a mod that prevents excessive server lag caused by large amounts of primed TNT.
It does this by limiting the number of TNT entities that can be active at the same time.

When the active TNT count exceeds the configured limit, additional TNT is placed into a queue and activated later, ensuring explosions remain manageable without overwhelming the server.

---

### Configuration
The mod provides two configurable options, which can be adjusted either manually in the config file or via an in-game command:

* **`maxPrimedTntAmount`:** — The maximum number of primed TNT entities allowed to exist simultaneously.

* **`maxQueueSize`:** — The maximum number of TNT entities that can be stored in the queue.

>⚠️ Default values are arbitrary and should be tuned for your specific server setup.
>The intention of the mod is to block TNT usage at levels that could otherwise cause the server to freeze.

You can adjust the configuration in-game using the command:<br>
`/tntqueue <option> <value>`<br>
Requires the `tntqueue.modify` permission or op.

In the example limit of primed tnt is up to 3 tnt. <br>
![](tnt.gif)

---
You can visit my little [contact card](https://somykos.github.io/web/), <br>
And you are welcome to support me via the following links:<br>
<a href="https://ko-fi.com/somyk">
<img src="https://raw.githubusercontent.com/somykOS/web/c03742bd86ca2ce0f6f39bcd3cfe683ad98926a2/public/external/kofi_s_logo_nolabel.svg" alt="ko-fi" width="100"/>
</a>
<a href="https://send.monobank.ua/jar/8RCzun35pC">
<img src="https://raw.githubusercontent.com/somykOS/web/5ac2e685429eb0cc369dc220ce3b93d2a22893c0/public/external/monobank_logo.svg" alt="monobank" width="80"/>
</a>
