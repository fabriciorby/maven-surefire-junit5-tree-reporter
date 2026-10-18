// the forks complete their classes in any order, the tree is the one of failure-summary
binding.setVariable('sortClassBlocks', true)
binding.setVariable('goldenScenario', 'failure-summary')
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
binding.setVariable('sortSummary', true)
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-summary.groovy'))
