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
package io.github.spleefx.command.parent;

import io.github.spleefx.arena.bow.BowSpleefArena;
import io.github.spleefx.arena.custom.ExtensionArena;
import io.github.spleefx.arena.spleef.SpleefArena;
import io.github.spleefx.arena.splegg.SpleggArena;
import io.github.spleefx.command.core.CommandHandler.ParentCommand;
import io.github.spleefx.command.parent.extensions.*;
import io.github.spleefx.command.parent.spleefx.*;

/**
 * An interface for containing all {@link ParentCommand}s
 */
public interface CommandsContainer {

    ParentCommand SPLEEFX = ParentCommand.create()
            .contain(new BalanceCommand())
            .contain(new BoosterCommand())
            .contain(new CoinsCommand())
            .contain(new ExtensionCommand())
            .contain(new MessagesCommand())
            .contain(new PerksCommand())
            .contain(new ReloadCommand())
            .contain(new HelpCommand());

    ParentCommand SPLEEF = ParentCommand.create()
            .contain(new ArenaCommand<>((key, name, regen, arenaType, e) -> new SpleefArena(key, name, regen, arenaType)))
            .contain(new HelpCommand())
            .contain(new JoinCommand())
            .contain(new JoinGUICommand())
            .contain(new LeaveCommand())
            .contain(new ForceStartCommand())
            .contain(new ListArenasCommand())
            .contain(new StatsCommand());

    ParentCommand BOW_SPLEEF = ParentCommand.create()
            .contain(new ArenaCommand<>((key, name, regen, arenaType, e) -> new BowSpleefArena(key, name, regen, arenaType)))
            .contain(new HelpCommand())
            .contain(new JoinCommand())
            .contain(new JoinGUICommand())
            .contain(new ForceStartCommand())
            .contain(new LeaveCommand())
            .contain(new ListArenasCommand())
            .contain(new StatsCommand());

    ParentCommand SPLEGG = ParentCommand.create()
            .contain(new ArenaCommand<>((key, name, regen, arenaType, e) -> new SpleggArena(key, name, regen, arenaType)))
            .contain(new HelpCommand())
            .contain(new JoinCommand())
            .contain(new JoinGUICommand())
            .contain(new LeaveCommand())
            .contain(new ForceStartCommand())
            .contain(new SpleggShopCommand())
            .contain(new ListArenasCommand())
            .contain(new StatsCommand());

    ParentCommand CUSTOM = ParentCommand.create()
            .contain(new ArenaCommand<>((key, name, regen, arenaType, e) -> new ExtensionArena(key, name, regen, e, arenaType)))
            .contain(new HelpCommand())
            .contain(new JoinCommand())
            .contain(new JoinGUICommand())
            .contain(new LeaveCommand())
            .contain(new ListArenasCommand())
            .contain(new StatsCommand());

}