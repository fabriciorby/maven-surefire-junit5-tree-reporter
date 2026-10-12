binding.setVariable('goldenScenario', 'nested-tree')
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
