// failure-summary's tests with maven-failsafe-plugin, run twice: the tree and the summary are the ones of failure-summary
binding.setVariable('goldenScenario', 'failure-summary')
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
binding.setVariable('summaryScenario', 'failure-summary')
binding.setVariable('executions', 2)
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-summary.groovy'))
