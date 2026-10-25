package com.triassicisland.security;

/** A wall screen that computers write text to. */
public class MonitorBlock extends WallDeviceBlock {
    public MonitorBlock(Properties properties) {
        super(properties, 3, 0, 0, 16, MonitorBlockEntity::new);
    }
}
