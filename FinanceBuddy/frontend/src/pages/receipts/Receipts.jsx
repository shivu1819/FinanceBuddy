import { useEffect, useMemo, useRef, useState } from 'react'
import { ArrowUpTrayIcon, CameraIcon, CheckCircleIcon, DocumentTextIcon, TrashIcon } from '@heroicons/react/24/outline'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import Sidebar from '../../components/dashboard/Sidebar'
import Navbar from '../../components/dashboard/Navbar'
import useAuth from '../../hooks/useAuth'
import { getCategories, createTransaction } from '../../services/transactionService'
import { scanReceipt } from '../../services/receiptService'

const acceptedTypes = ['image/jpeg', 'image/png', 'image/webp']
const acceptedExtensions = '.jpg,.jpeg,.png,.webp'
const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 })

const errorMessage = (error, fallback) => {
  const status = error.response?.status
  const message = error.response?.data?.message || error.response?.data?.error
  if (status === 401) return 'Your session has expired. Please sign in again.'
  if (status === 403) return 'You do not have permission to scan receipts.'
  if (status === 413) return 'Receipt image must not exceed 5 MB.'
  if (status === 400) return message || 'Please upload a valid JPG, PNG, or WEBP receipt image.'
  if (status === 422) return message || 'Receipt OCR could not read this image. Try a clearer receipt.'
  return message || (error.request ? 'Unable to reach the server. Please try again.' : fallback)
}

const valueOf = (data, ...keys) => keys.map((key) => data?.[key]).find((value) => value !== undefined && value !== null && value !== '') ?? ''

function normalizeDate(value) {
  if (!value) return ''
  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? String(value).slice(0, 10) : parsed.toISOString().slice(0, 10)
}

function normalizeResult(data) {
  const result = data?.result || data?.data || data || {}
  const suggested = valueOf(result, 'suggestedCategory', 'category', 'categoryName')
  const overallConfidence = valueOf(result, 'overallConfidence', 'confidence', 'ocrConfidence')
  return {
    merchantName: String(valueOf(result, 'merchantName', 'merchant', 'storeName') || ''),
    transactionDate: normalizeDate(valueOf(result, 'transactionDate', 'date', 'receiptDate')),
    transactionTime: String(valueOf(result, 'transactionTime', 'time') || ''),
    subtotalAmount: valueOf(result, 'subtotalAmount', 'subtotal'),
    totalAmount: valueOf(result, 'totalAmount', 'amount', 'total'),
    taxAmount: valueOf(result, 'taxAmount', 'tax', 'vat'),
    cgstAmount: valueOf(result, 'cgstAmount', 'cgst'),
    sgstAmount: valueOf(result, 'sgstAmount', 'sgst'),
    igstAmount: valueOf(result, 'igstAmount', 'igst'),
    currency: String(valueOf(result, 'currency', 'currencyCode') || ''),
    paymentMethod: String(valueOf(result, 'paymentMethod', 'payment') || ''),
    suggestedCategory: typeof suggested === 'object' ? String(valueOf(suggested, 'name', 'categoryName') || '') : String(suggested || ''),
    ocrConfidence: overallConfidence,
    merchantConfidence: valueOf(result, 'merchantConfidence'),
    dateConfidence: valueOf(result, 'dateConfidence'),
    totalConfidence: valueOf(result, 'totalConfidence'),
    taxConfidence: valueOf(result, 'taxConfidence'),
    itemConfidence: valueOf(result, 'itemConfidence'),
    subtotalConfidence: valueOf(result, 'subtotalConfidence'),
    categoryConfidence: valueOf(result, 'categoryConfidence'),
    financiallyConsistent: result.financiallyConsistent,
    validationMessage: String(result.validationMessage || ''),
    items: Array.isArray(result.items) ? result.items : [],
    rawText: String(valueOf(result, 'rawText', 'extractedText', 'text') || ''),
  }
}

function confidenceLabel(value) {
  if (value === '' || value === null || value === undefined) return 'Not provided'
  const number = Number(value)
  return `${(number <= 1 ? number * 100 : number).toFixed(0)}%`
}

function Field({ label, ...props }) {
  return <label><span className="mb-1.5 block text-sm font-semibold text-slate-700">{label}</span><input {...props} className="w-full rounded-xl border border-slate-200 px-3 py-3 text-sm outline-none focus:border-emerald-500 focus:ring-4 focus:ring-emerald-100" /></label>
}

function Receipts() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const [file, setFile] = useState(null)
  const [preview, setPreview] = useState('')
  const [result, setResult] = useState(null)
  const [categories, setCategories] = useState([])
  const [review, setReview] = useState({ merchantName: '', transactionDate: '', totalAmount: '', categoryId: '' })
  const [dragging, setDragging] = useState(false)
  const [scanning, setScanning] = useState(false)
  const [saving, setSaving] = useState(false)
  const [status, setStatus] = useState('image-selected')
  const inputRef = useRef(null)
  const navigate = useNavigate()
  const { user } = useAuth()

  useEffect(() => {
    getCategories()
      .then((items) => setCategories(items.filter((category) => !category.type || category.type === 'EXPENSE')))
      .catch(() => toast.error('Unable to load categories.'))
  }, [])
  useEffect(() => () => { if (preview) URL.revokeObjectURL(preview) }, [preview])

  const suggestedCategory = useMemo(() => {
    const name = result?.suggestedCategory?.toLowerCase()
    return name ? categories.find((category) => category.name?.toLowerCase() === name) : null
  }, [categories, result])

  const selectFile = (nextFile) => {
    if (!nextFile) return
    if (!acceptedTypes.includes(nextFile.type)) {
      toast.error('Please select a JPG, JPEG, PNG, or WEBP image.')
      return
    }
    if (preview) URL.revokeObjectURL(preview)
    setFile(nextFile)
    setPreview(URL.createObjectURL(nextFile))
    setResult(null)
    setStatus('image-selected')
  }

  const removeFile = () => {
    if (preview) URL.revokeObjectURL(preview)
    setFile(null)
    setPreview('')
    setResult(null)
    setStatus('empty')
    setReview({ merchantName: '', transactionDate: '', totalAmount: '', categoryId: '' })
  }

  const scan = async () => {
    if (!file || scanning) return
    setScanning(true)
    setStatus('preprocessing')
    try {
      setStatus('scanning')
      const scanned = normalizeResult(await scanReceipt(file))
      setResult(scanned)
      setReview({ merchantName: scanned.merchantName, transactionDate: scanned.transactionDate, totalAmount: scanned.totalAmount, categoryId: '' })
      setStatus(scanned.rawText || scanned.merchantName || scanned.totalAmount ? 'review' : 'no-text')
      if (scanned.rawText || scanned.merchantName || scanned.totalAmount) toast.success('Receipt scanned. Review the details before saving.')
      else toast.error('No readable text was detected. Try a clearer receipt image.')
    } catch (error) {
      setStatus(error.response?.status === 422 ? 'ocr-unavailable' : 'api-error')
      toast.error(errorMessage(error, 'Unable to scan the receipt.'))
    } finally {
      setScanning(false)
    }
  }

  const updateReview = (key, value) => setReview((current) => ({ ...current, [key]: value }))
  const saveTransaction = async (event) => {
    event.preventDefault()
    if (!review.merchantName.trim() || !review.transactionDate || Number(review.totalAmount) <= 0 || !review.categoryId) {
      toast.error('Complete the merchant, date, amount, and category before creating the transaction.')
      return
    }
    setSaving(true)
    try {
      await createTransaction({ description: review.merchantName.trim(), transactionDate: review.transactionDate, amount: Number(review.totalAmount), categoryId: Number(review.categoryId), transactionType: 'EXPENSE', paymentMethod: 'OTHER' })
      toast.success('Transaction created successfully.')
      removeFile()
      navigate('/transactions')
    } catch (error) {
      toast.error(errorMessage(error, 'Unable to create the transaction.'))
    } finally {
      setSaving(false)
    }
  }

  const handleDrop = (event) => { event.preventDefault(); setDragging(false); selectFile(event.dataTransfer.files?.[0]) }
  const scanLabel = scanning ? (status === 'preprocessing' ? 'Preparing image...' : 'Scanning receipt...') : 'Scan receipt'

  return <div className="flex min-h-screen bg-slate-50"><Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} /><div className="min-w-0 flex-1"><Navbar onMenuClick={() => setSidebarOpen(true)} user={user} /><main className="mx-auto max-w-6xl space-y-6 p-5 sm:p-8"><section><p className="text-sm font-medium text-emerald-600">Smart capture</p><h2 className="mt-1 text-2xl font-bold tracking-tight text-slate-950 sm:text-3xl">Receipt scanner</h2><p className="mt-2 text-sm text-slate-500">Scan a receipt, review what was found, and choose when to add it to your transactions.</p></section><section className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]"><div className="space-y-5"><div onDragOver={(event) => { event.preventDefault(); setDragging(true) }} onDragLeave={() => setDragging(false)} onDrop={handleDrop} className={`rounded-2xl border-2 border-dashed bg-white p-6 text-center transition ${dragging ? 'border-emerald-500 bg-emerald-50' : 'border-slate-300'}`}><input ref={inputRef} type="file" accept={acceptedExtensions} className="hidden" onChange={(event) => selectFile(event.target.files?.[0])} />{preview ? <div className="space-y-4"><img src={preview} alt="Receipt preview" className="mx-auto max-h-80 max-w-full rounded-xl object-contain shadow-sm" /><div className="flex flex-wrap justify-center gap-2"><button onClick={() => inputRef.current?.click()} className="inline-flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"><ArrowUpTrayIcon className="size-5" /> Replace image</button><button onClick={removeFile} className="inline-flex items-center gap-2 rounded-xl border border-rose-200 px-4 py-2.5 text-sm font-semibold text-rose-600 hover:bg-rose-50"><TrashIcon className="size-5" /> Remove</button></div></div> : <><span className="mx-auto grid size-14 place-items-center rounded-2xl bg-emerald-100 text-emerald-700"><CameraIcon className="size-7" /></span><h3 className="mt-4 font-bold text-slate-900">Upload a receipt</h3><p className="mt-2 text-sm text-slate-500">Drag and drop an image here, or choose one from your device.</p><button onClick={() => inputRef.current?.click()} className="mt-5 inline-flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white hover:bg-emerald-700"><ArrowUpTrayIcon className="size-5" /> Upload receipt</button><p className="mt-3 text-xs text-slate-400">JPG, JPEG, PNG, or WEBP</p></>}</div>{file && <button onClick={scan} disabled={scanning} className="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-slate-950 px-4 py-3 text-sm font-semibold text-white hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"><DocumentTextIcon className="size-5" /> {scanLabel}</button>}</div>{result ? <section className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm sm:p-6"><div className="flex items-start justify-between gap-4"><div><p className="text-sm font-medium text-emerald-600">{status === 'no-text' ? 'No text detected' : 'OCR complete'}</p><h3 className="mt-1 text-xl font-bold text-slate-950">Review receipt</h3></div><CheckCircleIcon className="size-7 text-emerald-600" /></div><div className="mt-5 grid gap-4 sm:grid-cols-2"><div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Detected total</p><p className="mt-1 text-lg font-bold text-slate-900">{result.totalAmount !== '' ? `${result.currency ? `${result.currency} ` : ''}${money.format(Number(result.totalAmount))}` : 'Please verify'}</p><p className="mt-1 text-xs text-slate-500">Confidence: {confidenceLabel(result.totalConfidence)}</p></div><div className="rounded-xl bg-slate-50 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Tax amount</p><p className="mt-1 text-lg font-bold text-slate-900">{result.taxAmount !== '' ? money.format(Number(result.taxAmount)) : 'Please verify'}</p><p className="mt-1 text-xs text-slate-500">Confidence: {confidenceLabel(result.taxConfidence)}</p></div><div className="rounded-xl border border-slate-100 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Merchant</p><p className="mt-1 font-semibold text-slate-800">{result.merchantName || 'Please verify'}</p><p className="mt-1 text-xs text-slate-500">Confidence: {confidenceLabel(result.merchantConfidence)}</p></div><div className="rounded-xl border border-slate-100 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Date / time</p><p className="mt-1 font-semibold text-slate-800">{result.transactionDate || 'Please verify'} {result.transactionTime}</p><p className="mt-1 text-xs text-slate-500">Confidence: {confidenceLabel(result.dateConfidence)}</p></div><div className="rounded-xl border border-slate-100 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Suggested category</p><p className="mt-1 font-semibold text-slate-800">{result.suggestedCategory || 'Please verify'}</p></div><div className="rounded-xl border border-slate-100 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Payment method</p><p className="mt-1 font-semibold text-slate-800">{result.paymentMethod || 'Please verify'}</p></div><div className="rounded-xl border border-slate-100 p-4 sm:col-span-2"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Overall OCR confidence</p><p className="mt-1 font-semibold text-slate-800">{confidenceLabel(result.ocrConfidence)}</p>{result.validationMessage && <p className="mt-1 text-sm text-amber-700">{result.validationMessage}</p>}</div></div>{result.items.length > 0 && <div className="mt-5 rounded-xl border border-slate-100 p-4"><p className="text-xs font-semibold uppercase tracking-wide text-slate-400">Detected items</p><div className="mt-3 divide-y divide-slate-100">{result.items.map((item, index) => <div key={`${item.itemName}-${index}`} className="flex items-center justify-between gap-3 py-2 text-sm"><span className="font-medium text-slate-800">{item.itemName}{item.quantity && Number(item.quantity) !== 1 ? ` × ${item.quantity}` : ''}</span><span className="text-slate-600">{item.lineTotal !== undefined && item.lineTotal !== null ? money.format(Number(item.lineTotal)) : 'Please verify'}</span></div>)}</div></div>}<form onSubmit={saveTransaction} className="mt-6 border-t border-slate-100 pt-6"><h4 className="font-bold text-slate-900">Create transaction</h4><p className="mt-1 text-sm text-slate-500">Review and edit these fields before saving. Nothing is created automatically.</p><div className="mt-4 grid gap-4 sm:grid-cols-2"><Field label="Merchant name" value={review.merchantName} onChange={(event) => updateReview('merchantName', event.target.value)} required /><Field label="Transaction date" type="date" value={review.transactionDate} onChange={(event) => updateReview('transactionDate', event.target.value)} required /><Field label="Total amount" type="number" min="0.01" step="0.01" value={review.totalAmount} onChange={(event) => updateReview('totalAmount', event.target.value)} required /><label><span className="mb-1.5 block text-sm font-semibold text-slate-700">Category</span><select value={review.categoryId || suggestedCategory?.id || ''} onChange={(event) => updateReview('categoryId', event.target.value)} required className="w-full rounded-xl border border-slate-200 bg-white px-3 py-3 text-sm"><option value="">Select expense category</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select></label></div><button type="submit" disabled={saving} className="mt-5 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-3 text-sm font-semibold text-white hover:bg-emerald-700 disabled:opacity-60">{saving ? 'Creating transaction...' : 'Create Transaction'}</button></form><details className="mt-5 rounded-xl bg-slate-50 p-4"><summary className="cursor-pointer text-sm font-semibold text-slate-700">View extracted raw text</summary><pre className="mt-3 max-h-56 overflow-auto whitespace-pre-wrap text-xs leading-5 text-slate-500">{result.rawText || 'No raw text was returned.'}</pre></details></section> : <section className="grid min-h-[360px] place-items-center rounded-2xl border border-slate-200 bg-white p-8 text-center shadow-sm"><div><span className="mx-auto grid size-14 place-items-center rounded-2xl bg-slate-100 text-slate-400"><DocumentTextIcon className="size-7" /></span><h3 className="mt-4 text-lg font-bold text-slate-800">Review results will appear here</h3><p className="mx-auto mt-2 max-w-sm text-sm text-slate-500">Upload a clear receipt image and scan it to see the extracted details.</p></div></section>}</section></main></div></div>
}

export default Receipts
