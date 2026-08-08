package com.pushoff.game.engine

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.pushoff.domain.model.PushUpRep

/**
 * LibGDX game engine for combat and boss battles.
 * Renders the game HUD over the camera preview.
 */
class PushoffGame : Game() {
    
    private lateinit var camera: OrthographicCamera
    private lateinit var stage: Stage
    
    // Game state
    var bossHp = 1000
    var bossMaxHp = 1000
    var playerHp = 100
    var playerMaxHp = 100
    var currentCombo = 0
    var maxCombo = 0
    
    // Callback for rep events from Android
    private var onRepReceived: ((PushUpRep) -> Unit)? = null
    
    override fun create() {
        camera = OrthographicCamera()
        camera.setToOrtho(true, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        
        stage = Stage(ScreenViewport(camera))
        Gdx.input.inputProcessor = stage
        
        // Initialize game objects (boss, player, effects)
        // This would be expanded with actual game entities
    }
    
    override fun render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        
        // Update and draw game objects
        stage.act(Math.min(Gdx.graphics.deltaTime, 1 / 30f))
        stage.draw()
        
        // Game logic updates would go here
        updateGameLogic()
    }
    
    private fun updateGameLogic() {
        // Update boss AI, particle effects, animations, etc.
    }
    
    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        camera.setToOrtho(true, width.toFloat(), height.toFloat())
    }
    
    override fun dispose() {
        stage.dispose()
    }
    
    /**
     * Called when a push-up rep is detected by the pose engine.
     * Deals damage to boss, updates combo, triggers effects.
     */
    fun onRepDetected(rep: PushUpRep) {
        if (rep.quality == com.pushoff.core.pose.PushUpDetector.RepQuality.INVALID) {
            return // No damage for invalid reps
        }
        
        // Deal damage to boss
        bossHp = (bossHp - rep.damage).coerceAtLeast(0)
        
        // Update combo
        if (rep.quality.ordinal >= 2) { // GOOD or PERFECT
            currentCombo++
            maxCombo = maxOf(maxCombo, currentCombo)
        } else {
            currentCombo = 0
        }
        
        // Trigger visual effects based on quality
        when (rep.quality) {
            com.pushoff.core.pose.PushUpDetector.RepQuality.PERFECT -> {
                spawnDamageNumber(rep.damage, isCritical = true)
                spawnParticles("perfect")
            }
            com.pushoff.core.pose.PushUpDetector.RepQuality.GOOD -> {
                spawnDamageNumber(rep.damage, isCritical = false)
                spawnParticles("good")
            }
            com.pushoff.core.pose.PushUpDetector.RepQuality.WEAK -> {
                spawnDamageNumber(rep.damage, isCritical = false)
            }
            else -> {}
        }
        
        // Check win condition
        if (bossHp <= 0) {
            onBossDefeated()
        }
    }
    
    private fun spawnDamageNumber(amount: Int, isCritical: Boolean) {
        // Spawn floating damage text animation
        // Would use LibGDX scene2d UI or custom rendering
    }
    
    private fun spawnParticles(effectType: String) {
        // Spawn particle effects using LibGDX ParticleEffect
    }
    
    private fun onBossDefeated() {
        // Handle boss defeat - victory screen, rewards, etc.
    }
    
    fun setBossHp(hp: Int, maxHp: Int) {
        bossHp = hp
        bossMaxHp = maxHp
    }
    
    fun setPlayerHp(hp: Int, maxHp: Int) {
        playerHp = hp
        playerMaxHp = maxHp
    }
    
    fun resetGame() {
        bossHp = bossMaxHp
        playerHp = playerMaxHp
        currentCombo = 0
        maxCombo = 0
    }
}
