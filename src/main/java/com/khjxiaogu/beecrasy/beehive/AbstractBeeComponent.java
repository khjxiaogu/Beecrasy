package com.khjxiaogu.beecrasy.beehive;

import java.util.List;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation;
import com.khjxiaogu.beecrasy.components.BeeHiveArgumentation.Builder;
import com.khjxiaogu.beecrasy.utils.ItemValidateHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.DelegatingResourceHandler;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class AbstractBeeComponent implements ValueIOSerializable{


	/**
	 * 将数据写入 NBT（支持客户端/服务端区分）。
	 * 服务端写入完整数据，客户端只写入界面相关数据。
	 * @param nbt      输出目标
	 * @param isClient 是否为客户端写入
	 */
	public void readCustomNBT(ValueInput nbt, boolean isClient) {
		if(!isClient) {
			nbt.readChild("inv", getInternInv());
			nbt.readChild("hive", hiveInfo);
			shouldWork=nbt.getBooleanOr("nextWork", false);
			arguments=nbt.read("arguments", BeeHiveArgumentation.CODEC).orElse(null);

			beginingTicks=nbt.getIntOr("cooldown", 0);
		}
		work=nbt.read("work", WorkBehaviour.CODEC).orElse(WorkBehaviour.MAUNAL);
		err=nbt.read("err", ErrCode.CODEC).orElse(ErrCode.OK);
	}

	/**
	 * 将数据写入 NBT（支持客户端/服务端区分）。
	 * 服务端写入完整数据，客户端只写入界面相关数据。
	 * @param nbt      输出目标
	 * @param isClient 是否为客户端写入
	 */
	public void writeCustomNBT(ValueOutput nbt, boolean isClient) {
		if(!isClient) {
			nbt.putChild("inv", getInternInv());
			nbt.putChild("hive", hiveInfo);
			nbt.putBoolean("nextWork", shouldWork);
			if(arguments!=null)
				nbt.store("arguments", BeeHiveArgumentation.CODEC, arguments);
			nbt.putInt("cooldown", beginingTicks);
		}
		nbt.store("work", WorkBehaviour.CODEC, work);
		nbt.store("err", ErrCode.CODEC, err);
	}

	/**
	 * 将完整数据序列化到 ValueOutput 中。
	 * @param output 输出目标
	 */
	public void serialize(ValueOutput output) {
		writeCustomNBT(output,false);
	}
	/**
	 * 从 ValueInput 中反序列化恢复完整数据。
	 * @param input 输入源
	 */
	public void deserialize(ValueInput input) {
		readCustomNBT(input, false);
	}
	/**
	 * 将客户端同步数据序列化到 ValueOutput 中。
	 * 客户端序列化只包含部分数据（工作模式、错误码等），不包含完整库存和蜂巢状态。
	 * @param output 输出目标
	 */
	public void serializeClient(ValueOutput output) {
		writeCustomNBT(output,true);
	}
	/**
	 * 从 ValueInput 中反序列化恢复客户端同步数据。
	 * @param input 输入源
	 */
	public void deserializeClient(ValueInput input) {
		readCustomNBT(input, true);
	}

	/**
	 * 验证额外槽位是否允许放入指定物品。
	 * 子类可重写此方法以添加自定义槽位规则。
	 * @param index    额外槽位索引
	 * @param resource 待验证的物品资源
	 * @return 如果允许放入则返回 true
	 */
	public abstract boolean isValidForExtra(int index, ItemResource resource);

	/** 内部物品栈资源句柄（所有槽位的底层存储）。 */
	protected ItemStacksResourceHandler internInv;
	/** 外部访问委托（用于管道/漏斗等自动化设备的外部输入验证）。 */
	protected DelegatingResourceHandler<ItemResource> externInv;
	/** 产物输出委托（限制仅允许从巢脾槽位提取产物）。 */
	protected DelegatingResourceHandler<ItemResource> productInv;
	/** 蜂后槽位列表。 */
	protected List<HiveSlot> queenSlot;
	/** 巢脾/产物槽位列表。 */
	protected List<HiveSlot> combSlot;
	/** 雄蜂槽位列表。 */
	protected List<HiveSlot> droneSlot;
	/** 额外槽位列表（用于模组扩展）。 */
	protected List<HiveSlot> extraSlot;
	/** 蜂巢工作周期处理器。 */
	public final BeeHiveHandler hiveInfo;
	/** 参数增强项（由物品如信息素提供的临时参数加成）。 */
	protected BeeHiveArgumentation arguments;
	/** 是否应开始工作（由工作模式和内容变更触发）。 */
	protected boolean shouldWork;
	/** 数据是否已变更（需要保存到磁盘）。 */
	protected boolean changed;
	/** 工作开始前的冷却 tick 计数。 */
	protected int beginingTicks = 0;
	/** 当前工作模式（默认为手动）。 */
	public WorkBehaviour work = WorkBehaviour.MAUNAL;
	/** 当前错误/状态码。 */
	public ErrCode err = ErrCode.OK;
	/** 冷却总时长（tick 数）。 */
	public static final int COOLDOWN_TIME = 100;

	public AbstractBeeComponent(int queen,int drone,int comb,int extra) {
		super();
		internInv = new ItemStacksResourceHandler(queen+drone+comb+extra) {
			@Override
			protected void onContentsChanged(int slot, ItemStack stack) {
				if(slot<queen&&this.getAmountAsInt(slot)>0&&!hiveInfo.isWorking()) {
					shouldWork=true;
				}
				beginingTicks=0;
				setChanged();
				super.onContentsChanged(slot, stack);
			}
			
		};
		externInv=new DelegatingResourceHandler<>(internInv) {

			@Override
			public boolean isValid(int index, ItemResource resource) {
				if(index<queen)
					return ItemValidateHelper.isQueen(resource.toStack());
				if(index<queen+drone)
					return ItemValidateHelper.isDrone(resource.toStack());
				if(index<queen+drone+comb)
					return ItemValidateHelper.isComb(resource.toStack());
				return isValidForExtra(index-(queen+drone+comb),resource);
			}
			
		};
		productInv=new RangedResourceHandler<>(internInv,queen+drone,queen+drone+comb) {

			@Override
			public ItemResource getResource(int index) {
				ItemResource ir= super.getResource(index);
				if(!ItemValidateHelper.isComb(ir.toStack()))
					return ir;
				return ItemResource.EMPTY;
			}

			@Override
			public long getAmountAsLong(int index) {
				if(getResource(index).isEmpty())
					return 0;
				return super.getAmountAsLong(index);
			}

			@Override
			public boolean isValid(int index, ItemResource resource) {
				return false;
			}

			@Override
			public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
				return 0;
			}

			@Override
			public int insert(ItemResource resource, int amount, TransactionContext transaction) {
				return 0;
			}
		};
		this.hiveInfo = createHiveInfo(queen,drone,comb,extra);
	}
	public abstract BeeHiveHandler createHiveInfo(int queen,int drone,int comb,int extra);
	public void setShouldWork(boolean shouldWork) {
		this.shouldWork = shouldWork;
	}

	/**
	 * 构建蜂巢参数集构建器。
	 * 从世界中获取当前环境参数，如果存在参数增强项则应用其提供的参数覆盖。
	 * @param serverLevel  服务端世界
	 * @param worldPosition 蜂巢方块位置
	 * @return 参数集构建器
	 */
	public BeeHiveParameterSet.Builder buildParams(ServerLevel serverLevel, BlockPos worldPosition) {
		BeeHiveParameterSet.Builder builder= new BeeHiveParameterSet.Builder(serverLevel,worldPosition);
		if(arguments!=null)
			builder.setParams(arguments.params());
		return builder;
	}

	/**
	 * 构建参数增强项。
	 * 子类可重写此方法以提供自定义的参数增强方式。
	 * @param serverLevel  服务端世界
	 * @param worldPosition 蜂巢方块位置
	 * @param root         事务上下文
	 * @return 参数增强项构建器
	 */
	public Builder buildArgumentation(ServerLevel level, BlockPos worldPosition, TransactionContext root) {
	
		Builder builder= new BeeHiveArgumentation.Builder();
		return builder;
	}

	/**
	 * 检查是否可以开始工作。
	 * 验证条件包括：工作模式、蜂后数量、雄蜂数量、槽位内容格式等。
	 * 同时设置错误码 {@link #err} 指示具体失败原因。
	 * @return 如果可以开始工作则返回 true
	 */
	protected abstract boolean canBeginWork();

	/**
	 * 开始一个繁殖/工作周期。
	 * 从槽位中提取蜂后和雄蜂的基因组，构建参数集，验证环境适宜性，
	 * 然后通知 {@link BeeHiveHandler} 开始工作。
	 * @param serverLevel  服务端世界
	 * @param worldPosition 蜂巢方块位置
	 * @return 如果成功启动工作周期则返回 true
	 */
	protected abstract boolean beginGrowth(ServerLevel serverLevel, BlockPos worldPosition);

	/**
	 * 获取容器数据接口，用于与 GUI 菜单同步。
	 * 包含 4 个数据槽位：
	 * <ul>
	 *   <li>0: 错误码 {@link ErrCode} 的序数</li>
	 *   <li>1: 工作模式 {@link WorkBehaviour} 的序数</li>
	 *   <li>2: 当前进度（冷却中则为冷却 time，工作中则为工作进度）</li>
	 *   <li>3: 最大进度（冷却中则为冷却总时长，工作中则为最大工作进度）</li>
	 * </ul>
	 * @return 容器数据接口
	 */
	public ContainerData containerData() {
		return new ContainerData() {
	
			@Override
			public int get(int dataId) {
				switch(dataId) {
				case 0:return err.ordinal();
				case 1:return work.ordinal();
				case 2:return beginingTicks>0?beginingTicks:hiveInfo.getProcess();
				case 3:return beginingTicks>0?COOLDOWN_TIME:hiveInfo.getProcessMax();
				}
				return 0;
			}
	
			@Override
			public void set(int dataId, int value) {
				switch(dataId) {
				case 0:err=ErrCode.values()[value];break;
				case 1:work=WorkBehaviour.values()[value];break;
				}
			}
	
			@Override
			public int getCount() {
				return 4;
			}
			
		};
	}

	/**
	 * 主 tick 方法，驱动蜂巢的完整工作逻辑。
	 * 根据工作模式设置 shouldWork 标志，管理冷却计时，调用 {@link BeeHiveHandler#tick} 推进工作，
	 * 并根据工作状态设置错误码。
	 * @param serverLevel  服务端世界
	 * @param worldPosition 蜂巢方块位置
	 * @param speed         工作速度倍率
	 * @param hasRedstone   是否接收到红石信号
	 */
	public void tick(ServerLevel serverLevel, BlockPos worldPosition, int speed, boolean hasRedstone) {
		switch(work) {
		case AUTO:shouldWork=true;break;
		case REDSTONE:shouldWork=hasRedstone;break;
		default:
			break;
		}
		
		if(beginingTicks>0) {
			beginingTicks+=speed;
			if(beginingTicks>=COOLDOWN_TIME) {
				beginingTicks=0;
				if(canBeginWork()) {
					shouldWork=false;
					if(beginGrowth(serverLevel, worldPosition)) {
						setChanged();
					}
				}
			}
			return;
		}else if(hiveInfo.isWorking()) {
			err=ErrCode.OK;
			BeeHiveParameterSet params=buildParams(serverLevel, worldPosition).build();
			hiveInfo.tick(params,speed);
			setChanged();
			if(hiveInfo.isBadEnvironment())
				err=ErrCode.INVALID_ENVIRONMENT;
			else if(hiveInfo.isNoFlower()) 
				err=ErrCode.NO_FLOWER;
			else if(hiveInfo.isBlocked())
				err=ErrCode.EMPTY_QUEEN;
			else if(hiveInfo.isNoBiotope())
				err=ErrCode.NO_BIOTOPE;
			return;
		}else if(shouldWork) {
			if(canBeginWork()) {
				beginingTicks=1;
			}
		}else if(err==ErrCode.OK) {
			err=ErrCode.MANUAL_HALT;
		}
		hiveInfo.tickNotWorking(serverLevel);
	}

	/**
	 * 标记数据已变更，需要保存到磁盘。
	 */
	public void setChanged() {
		this.changed=true;
	}

	/**
	 * 获取内部物品栈资源句柄。
	 * @return 内部资源句柄
	 */
	public ItemStacksResourceHandler getInternInv() {
		return internInv;
	}

	/**
	 * 获取外部访问委托资源句柄（用于管道/漏斗等自动化设备）。
	 * @return 外部访问委托
	 */
	public DelegatingResourceHandler<ItemResource> getExternInv() {
		return externInv;
	}

	/**
	 * 获取产物输出委托资源句柄（用于从巢脾提取产物）。
	 * @return 产物输出委托
	 */
	public DelegatingResourceHandler<ItemResource> getProductInv() {
		return productInv;
	}

	/**
	 * 检查数据是否已变更。
	 * @return 如果自上次保存以来有变更则返回 true
	 */
	public boolean isChanged() {
		return changed;
	}

	/**
	 * 设置数据变更标志。
	 * @param changed 数据是否已变更
	 */
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public List<HiveSlot> getQueenSlot() {
		return queenSlot;
	}

	public List<HiveSlot> getCombSlot() {
		return combSlot;
	}

	public List<HiveSlot> getDroneSlot() {
		return droneSlot;
	}

	public List<HiveSlot> getExtraSlot() {
		return extraSlot;
	}

}