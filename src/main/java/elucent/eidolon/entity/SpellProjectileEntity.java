package elucent.eidolon.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.UUID;

public abstract class SpellProjectileEntity extends Entity {
    UUID casterId = null;

    public SpellProjectileEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public Entity shoot(double x, double y, double z, double vx, double vy, double vz, UUID caster) {
        setPosition(x, y, z);
        setMotion(vx, vy, vz);
        casterId = caster;
        velocityChanged = true;
        return this;
    }

    @Override
    public void tick() {
        Vector3d motion = getMotion();
        setMotion(motion.x * 0.96, (motion.y > 0 ? motion.y * 0.96 : motion.y) - 0.03f, motion.z * 0.96);

        super.tick();

        if (!world.isRemote) {
            RayTraceResult ray = ProjectileHelper.func_234618_a_(this, (e) -> !e.isSpectator() && e.canBeCollidedWith() && !e.getUniqueID().equals(casterId));
            if (ray.getType() == RayTraceResult.Type.ENTITY) {
                onImpact(ray, ((EntityRayTraceResult)ray).getEntity());
            }
            else if (ray.getType() == RayTraceResult.Type.BLOCK) {
                onImpact(ray);
            }
        }

        Vector3d pos = getPositionVec();
        prevPosX = pos.x;
        prevPosY = pos.y;
        prevPosZ = pos.z;
        setPosition(pos.x + motion.x, pos.y + motion.y, pos.z + motion.z);
    }

    protected abstract void onImpact(RayTraceResult ray, Entity target);
    protected abstract void onImpact(RayTraceResult ray);

    @Override
    protected void registerData() {
        //
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        casterId = compound.contains("caster") ? compound.getUniqueId("caster") : null;
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        if (casterId != null) compound.putUniqueId("caster", casterId);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
