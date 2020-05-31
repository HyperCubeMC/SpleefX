/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.util.game;

import com.google.gson.annotations.Expose;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * A simple {@link Vector} wrapper
 */
public class VectorHolder {

    @Expose
    private double x;

    @Expose
    private double y;

    @Expose
    private double z;

    @Expose
    private boolean usePlayerOriginalVector = false;

    @Expose
    private double multiply = 1;

    @Expose
    private double divide = 1;

    @Expose
    private double addition = 0;

    @Expose
    private double subtract = 0;

    public VectorHolder(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector getVector(Player player) {
        if (usePlayerOriginalVector) {
            Vector original = player.getLocation().getDirection();
            double x = original.getX() * multiply / divide + addition - subtract;
            double y = original.getY() * multiply / divide + addition - subtract;
            double z = original.getZ() * multiply / divide + addition - subtract;
            return new Vector(x, y, z);
        }
        double x = this.x * multiply / divide + addition - subtract;
        double y = this.y * multiply / divide + addition - subtract;
        double z = this.z * multiply / divide + addition - subtract;
        return new Vector(x, y, z);
    }

}