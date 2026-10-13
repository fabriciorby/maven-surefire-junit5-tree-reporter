// the forks complete their classes in any order
binding.setVariable('sortClassBlocks', true)
binding.setVariable('goldenScenario', 'parallel-tree')
new GroovyShell(binding).evaluate(new File(itSourceDir, 'verify-tree.groovy'))
