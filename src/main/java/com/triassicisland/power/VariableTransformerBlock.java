package com.triassicisland.power;

/** A transformer whose ratio can be changed (sneak + right-click): 2, 4, 8 or 16. */
public class VariableTransformerBlock extends TransformerBlock {
    public VariableTransformerBlock(Properties properties) {
        super(properties, VariableTransformerBlockEntity::new);
    }
}
