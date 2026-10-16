### Test results

11 tests: 6 passed, 4 failed, 1 skipped

<details>
<summary>Beta, failures in the class and in a nested class: 2 failed</summary>

```text
┬─ Beta, failures in the class and in a nested class - <time>
│  ├─ ✘ fails in the class - <time>
│  └─ ✔ passes - <time>
└─ ── Inner - <time>
      ├─ ✘ fails in the nested class - <time>
      └─ ✔ passes in the nested class - <time>
```

</details>

<details>
<summary>Gamma, an error, a failure and a skip: 2 failed</summary>

```text
── Gamma, an error, a failure and a skip - <time>
   ├─ ✘ throws an error - <time>
   ├─ ✘ fails - <time>
   └─ ↷ is skipped (void sample.GammaTest.skipped() is @Disabled) - <time>
```

</details>

Classes without failures: `Alpha, all green`, `sample.DeltaTest`

