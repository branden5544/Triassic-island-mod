package com.triassicisland.security;

public class SecurityCameraBlock extends WallDeviceBlock {
    public SecurityCameraBlock(Properties properties) {
        super(properties, 10, 4, 6, 14, SecurityCameraBlockEntity::new);
    }
}
